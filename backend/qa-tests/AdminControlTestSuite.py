#!/usr/bin/env python3
"""
Professional QA Test Suite for SuperMalleMevan Admin Controls
=============================================================
Tests all administrative control endpoints with real-world scenarios.
Admin credentials: admin@supermalle.com / admin123

Usage:
  python3 AdminControlTestSuite.py --base-url http://localhost:8080
  python3 AdminControlTestSuite.py --json > report.json
"""

import urllib.request
import urllib.parse
import json
import sys
import time
import argparse
from typing import Optional, Dict, Any, List
from dataclasses import dataclass, asdict
from enum import Enum

# ============================================================================
# Configuration
# ============================================================================
BASE_URL = "http://localhost:8080"
ADMIN_EMAIL = "admin@supermalle.com"
ADMIN_PASSWORD = "admin123"
TIMEOUT = 30

# ============================================================================
# Test Result Types
# ============================================================================
class TestStatus(Enum):
    PASS = "✅ PASS"
    FAIL = "❌ FAIL"
    SKIP = "⚠️ SKIP"
    ERROR = "💥 ERROR"

@dataclass
class TestResult:
    name: str
    status: TestStatus
    message: str
    duration_ms: float
    endpoint: Optional[str] = None

# ============================================================================
# HTTP Client Helper
# ============================================================================
class AdminAPIClient:
    """Authenticated HTTP client for admin API testing"""
    
    def __init__(self, base_url: str, email: str, password: str):
        self.base_url = base_url.rstrip('/')
        self.token: Optional[str] = None
        self._authenticate(email, password)
    
    def _authenticate(self, email: str, password: str):
        """Login and obtain JWT token"""
        url = f"{self.base_url}/api/v1/auth/login"
        data = json.dumps({"email": email, "password": password}).encode()
        req = urllib.request.Request(url, data=data, method='POST')
        req.add_header('Content-Type', 'application/json')
        
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            result = json.loads(resp.read().decode())
            self.token = result.get('token')
            if not self.token:
                raise RuntimeError("Authentication failed: no token received")
    
    def _headers(self) -> Dict[str, str]:
        return {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.token}'
        }
    
    def request(self, method: str, path: str, 
                payload: Optional[Dict] = None,
                params: Optional[Dict] = None) -> Dict[str, Any]:
        """Make authenticated request and return response dict"""
        url = f"{self.base_url}{path}"
        if params:
            url += '?' + urllib.parse.urlencode(params)
        
        data = json.dumps(payload).encode() if payload else None
        req = urllib.request.Request(url, data=data, method=method, headers=self._headers())
        
        try:
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                return {
                    'status': resp.status,
                    'data': json.loads(resp.read().decode()) if resp.status != 204 else None,
                }
        except urllib.error.HTTPError as e:
            return {
                'status': e.code,
                'error': e.read().decode() if e.fp else str(e),
                'data': None
            }
        except Exception as e:
            return {'status': 0, 'error': str(e), 'data': None}
    
    def get(self, path: str, params: Optional[Dict] = None) -> Dict:
        return self.request('GET', path, params=params)
    
    def post(self, path: str, payload: Dict) -> Dict:
        return self.request('POST', path, payload=payload)
    
    def put(self, path: str, payload: Dict) -> Dict:
        return self.request('PUT', path, payload=payload)
    
    def patch(self, path: str, payload: Optional[Dict] = None) -> Dict:
        return self.request('PATCH', path, payload=payload)
    
    def delete(self, path: str) -> Dict:
        return self.request('DELETE', path)

# ============================================================================
# Test Cases
# ============================================================================
class AdminControlTests:
    """Professional QA test suite for admin controls"""
    
    def __init__(self, client: AdminAPIClient):
        self.client = client
        self.results: List[TestResult] = []
        self._test_data: Dict[str, Any] = {}
    
    def _run(self, name: str, endpoint: str, test_fn):
        """Execute a test and record result"""
        start = time.time()
        try:
            result = test_fn()
            status = TestStatus.PASS if result else TestStatus.FAIL
            msg = "Test passed" if result else "Assertion failed"
        except AssertionError as e:
            status = TestStatus.FAIL
            msg = f"Assertion: {e}"
        except Exception as e:
            status = TestStatus.ERROR
            msg = f"Exception: {type(e).__name__}: {e}"
        
        duration = (time.time() - start) * 1000
        self.results.append(TestResult(name, status, msg, duration, endpoint))
        return status == TestStatus.PASS
    
    # -------------------------------------------------------------------------
    # USER MANAGEMENT TESTS
    # -------------------------------------------------------------------------
    
    def test_user_list_pagination(self) -> bool:
        """TC-USER-001: Admin can list users with pagination"""
        resp = self.client.get('/api/v1/admin/users', {'page': 0, 'size': 5})
        assert resp['status'] == 200, f"Expected 200, got {resp['status']}"
        data = resp.get('data', {})
        assert 'items' in data, "Response missing 'items' field"
        assert 'total' in data, "Response missing 'total' field"
        assert len(data['items']) <= 5, "Pagination size not respected"
        return True
    
    def test_user_search(self) -> bool:
        """TC-USER-002: Admin can search users by name/email"""
        resp = self.client.get('/api/v1/admin/users', {'search': 'admin', 'page': 0, 'size': 10})
        assert resp['status'] == 200
        items = resp.get('data', {}).get('items', [])
        assert len(items) >= 1, "Search should return at least admin user"
        return True
    
    def test_user_get_by_id(self) -> bool:
        """TC-USER-003: Admin can fetch single user by ID"""
        list_resp = self.client.get('/api/v1/admin/users', {'page': 0, 'size': 1})
        user_id = list_resp['data']['items'][0]['id']
        resp = self.client.get(f'/api/v1/admin/users/{user_id}')
        assert resp['status'] == 200
        assert resp['data']['id'] == user_id
        return True
    
    def test_user_update(self) -> bool:
        """TC-USER-004: Admin can update user profile"""
        list_resp = self.client.get('/api/v1/admin/users', {'page': 0, 'size': 1})
        user_id = list_resp['data']['items'][0]['id']
        payload = {"name": f"Test User {int(time.time())}"}
        resp = self.client.put(f'/api/v1/admin/users/{user_id}', payload)
        assert resp['status'] == 200
        assert resp['data']['name'] == payload['name']
        return True
    
    def test_user_toggle_active(self) -> bool:
        """TC-USER-005: Admin can toggle user active status"""
        test_email = f"test+{int(time.time())}@example.com"
        reg_payload = {"name": "Test Toggle User", "email": test_email, "password": "TestPass123!", "phone": "+1234567890"}
        self.client.post('/api/v1/auth/register', reg_payload)
        search_resp = self.client.get('/api/v1/admin/users', {'search': test_email})
        user_id = search_resp['data']['items'][0]['id']
        resp = self.client.patch(f'/api/v1/admin/users/{user_id}/toggle-active')
        assert resp['status'] == 200
        assert resp['data']['isActive'] == False
        resp = self.client.patch(f'/api/v1/admin/users/{user_id}/toggle-active')
        assert resp['status'] == 200
        assert resp['data']['isActive'] == True
        self._test_data['test_user_id'] = user_id
        return True
    
    def test_user_password_reset(self) -> bool:
        """TC-USER-006: Admin can reset user password"""
        user_id = self._test_data.get('test_user_id')
        if not user_id:
            test_email = f"reset+{int(time.time())}@example.com"
            reg_payload = {"name": "Reset Test User", "email": test_email, "password": "OldPass123!", "phone": "+1234567890"}
            self.client.post('/api/v1/auth/register', reg_payload)
            search_resp = self.client.get('/api/v1/admin/users', {'search': test_email})
            user_id = search_resp['data']['items'][0]['id']
            self._test_data['test_user_id'] = user_id
        payload = {"newPassword": "NewSecurePass456!"}
        resp = self.client.post(f'/api/v1/admin/users/{user_id}/reset-password', payload)
        assert resp['status'] == 200
        return True
    
    def test_user_cannot_delete_self(self) -> bool:
        """TC-USER-007: Admin cannot delete their own account (security)"""
        me_resp = self.client.get('/api/v1/auth/me')
        admin_id = me_resp['data']['id']
        resp = self.client.delete(f'/api/v1/admin/users/{admin_id}')
        assert resp['status'] in [400, 403, 404], f"Expected protection, got {resp['status']}"
        return True
    
    # -------------------------------------------------------------------------
    # MENU MANAGEMENT TESTS
    # -------------------------------------------------------------------------
    
    def test_menu_list_all(self) -> bool:
        """TC-MENU-001: Admin can list all menu items with pagination"""
        resp = self.client.get('/api/v1/admin/menu', {'page': 0, 'size': 10})
        assert resp['status'] == 200
        data = resp.get('data', {})
        assert 'items' in data
        return True
    
    def test_menu_create(self) -> bool:
        """TC-MENU-002: Admin can create new menu item"""
        cat_resp = self.client.get('/api/v1/admin/categories')
        category_id = cat_resp['data'][0]['id'] if cat_resp['data'] else 1
        payload = {
            "name": f"QA Test Item {int(time.time())}",
            "description": "Automated test menu item",
            "price": 19.99,
            "categoryId": category_id,
            "imageUrl": "https://example.com/test.jpg",
            "available": True,
            "preparationTimeMinutes": 15,
            "spiceLevel": 2,
            "isVegetarian": True,
            "customizations": [{"name": "Size", "required": True, "multiSelect": False, "options": [{"name": "Small", "priceModifier": 0}, {"name": "Large", "priceModifier": 5.00}]}]
        }
        resp = self.client.post('/api/v1/admin/menu', payload)
        assert resp['status'] == 200, f"Create failed: {resp}"
        assert resp['data']['name'] == payload['name']
        self._test_data['test_menu_id'] = resp['data']['id']
        return True
    
    def test_menu_update(self) -> bool:
        """TC-MENU-003: Admin can update menu item"""
        menu_id = self._test_data.get('test_menu_id')
        if not menu_id:
            self.test_menu_create()
            menu_id = self._test_data['test_menu_id']
        payload = {"name": f"Updated QA Item {int(time.time())}", "price": 24.99, "description": "Updated description for testing"}
        resp = self.client.put(f'/api/v1/admin/menu/{menu_id}', payload)
        assert resp['status'] == 200
        assert resp['data']['name'] == payload['name']
        assert resp['data']['price'] == payload['price']
        return True
    
    def test_menu_toggle_availability(self) -> bool:
        """TC-MENU-004: Admin can toggle menu item availability"""
        menu_id = self._test_data.get('test_menu_id')
        if not menu_id:
            self.test_menu_create()
            menu_id = self._test_data['test_menu_id']
        resp = self.client.patch(f'/api/v1/admin/menu/{menu_id}/toggle-availability')
        assert resp['status'] == 200
        assert resp['data']['isAvailable'] == False
        resp = self.client.patch(f'/api/v1/admin/menu/{menu_id}/toggle-availability')
        assert resp['status'] == 200
        assert resp['data']['isAvailable'] == True
        return True
    
    def test_menu_delete(self) -> bool:
        """TC-MENU-005: Admin can soft-delete menu item"""
        menu_id = self._test_data.get('test_menu_id')
        if not menu_id:
            self.test_menu_create()
            menu_id = self._test_data['test_menu_id']
        resp = self.client.delete(f'/api/v1/admin/menu/{menu_id}')
        assert resp['status'] == 200
        avail_resp = self.client.get('/api/v1/menu', {'page': 0, 'size': 100})
        item_ids = [i['id'] for i in avail_resp['data'].get('items', [])]
        assert menu_id not in item_ids, "Deleted item should not appear in available menu"
        return True
    
    def test_menu_validation_required_fields(self) -> bool:
        """TC-MENU-006: API validates required fields on create"""
        payload = {"name": ""}
        resp = self.client.post('/api/v1/admin/menu', payload)
        assert resp['status'] == 400, "Should reject invalid payload"
        return True
    
    # -------------------------------------------------------------------------
    # COUPON/DISCOUNT TESTS
    # -------------------------------------------------------------------------
    
    def test_coupon_create(self) -> bool:
        """TC-COUPON-001: Admin can create discount coupon"""
        payload = {
            "code": f"QA{int(time.time())}",
            "discountType": "PERCENTAGE",
            "discountValue": 15.0,
            "minOrderAmount": 25.00,
            "maxDiscount": 10.00,
            "usageLimit": 100,
            "startDate": "2025-01-01T00:00:00",
            "endDate": "2025-12-31T23:59:59",
            "isActive": True
        }
        resp = self.client.post('/api/v1/admin/coupons', payload)
        assert resp['status'] == 200
        assert resp['data']['code'] == payload['code']
        self._test_data['test_coupon_id'] = resp['data']['id']
        return True
    
    def test_coupon_update(self) -> bool:
        """TC-COUPON-002: Admin can update coupon"""
        coupon_id = self._test_data.get('test_coupon_id')
        if not coupon_id:
            self.test_coupon_create()
            coupon_id = self._test_data['test_coupon_id']
        payload = {"discountValue": 20.0, "usageLimit": 50}
        resp = self.client.put(f'/api/v1/admin/coupons/{coupon_id}', payload)
        assert resp['status'] == 200
        assert resp['data']['discountValue'] == 20.0
        return True
    
    def test_coupon_delete(self) -> bool:
        """TC-COUPON-003: Admin can delete coupon"""
        coupon_id = self._test_data.get('test_coupon_id')
        if not coupon_id:
            self.test_coupon_create()
            coupon_id = self._test_data['test_coupon_id']
        resp = self.client.delete(f'/api/v1/admin/coupons/{coupon_id}')
        assert resp['status'] == 200
        return True
    
    # -------------------------------------------------------------------------
    # CATEGORY TESTS
    # -------------------------------------------------------------------------
    
    def test_category_list(self) -> bool:
        """TC-CAT-001: Admin can list categories"""
        resp = self.client.get('/api/v1/admin/categories')
        assert resp['status'] == 200
        assert isinstance(resp['data'], list)
        return True
    
    def test_category_create_update_delete(self) -> bool:
        """TC-CAT-002: Full category CRUD cycle"""
        payload = {"name": f"QA Category {int(time.time())}", "description": "Test category"}
        resp = self.client.post('/api/v1/admin/categories', payload)
        assert resp['status'] == 200
        cat_id = resp['data']['id']
        update_payload = {"description": "Updated test category"}
        resp = self.client.put(f'/api/v1/admin/categories/{cat_id}', update_payload)
        assert resp['status'] == 200
        resp = self.client.delete(f'/api/v1/admin/categories/{cat_id}')
        assert resp['status'] == 200
        return True
    
    # -------------------------------------------------------------------------
    # DASHBOARD TESTS
    # -------------------------------------------------------------------------
    
    def test_dashboard_stats(self) -> bool:
        """TC-DASH-001: Admin can fetch dashboard statistics"""
        resp = self.client.get('/api/v1/admin/dashboard/stats')
        assert resp['status'] == 200
        data = resp.get('data', {})
        required_fields = ['totalRevenue', 'totalOrders', 'activeOrders', 'totalCustomers']
        for field in required_fields:
            assert field in data, f"Missing field: {field}"
        return True
    
    def test_dashboard_charts(self) -> bool:
        """TC-DASH-002: Admin can fetch chart data"""
        resp = self.client.get('/api/v1/admin/dashboard/charts')
        assert resp['status'] == 200
        assert isinstance(resp.get('data'), (list, dict))
        return True
    
    def test_dashboard_top_items(self) -> bool:
        """TC-DASH-003: Admin can fetch top-selling items"""
        resp = self.client.get('/api/v1/admin/dashboard/top-items')
        assert resp['status'] == 200
        data = resp.get('data', [])
        assert isinstance(data, list)
        return True
    
    # -------------------------------------------------------------------------
    # SECURITY & EDGE CASES
    # -------------------------------------------------------------------------
    
    def test_unauthorized_access_blocked(self) -> bool:
        """TC-SEC-001: Non-admin requests are rejected"""
        url = f"{self.client.base_url}/api/v1/admin/users"
        req = urllib.request.Request(url, headers={'Content-Type': 'application/json'})
        try:
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                return False
        except urllib.error.HTTPError as e:
            assert e.code in [401, 403], f"Expected 401/403, got {e.code}"
            return True
    
    def test_invalid_id_format_rejected(self) -> bool:
        """TC-SEC-002: Invalid ID formats are rejected"""
        resp = self.client.get('/api/v1/admin/users/invalid-id')
        assert resp['status'] in [400, 404], f"Expected error for invalid ID, got {resp['status']}"
        return True
    
    def test_pagination_bounds(self) -> bool:
        """TC-EDGE-001: Pagination handles edge cases"""
        resp = self.client.get('/api/v1/admin/users', {'page': -1, 'size': 10})
        assert resp['status'] == 200
        resp = self.client.get('/api/v1/admin/users', {'page': 9999, 'size': 10})
        assert resp['status'] == 200
        assert len(resp['data'].get('items', [])) == 0
        return True
    
    # -------------------------------------------------------------------------
    # Test Runner
    # -------------------------------------------------------------------------
    
    def run_all(self) -> Dict[str, Any]:
        """Execute all tests and return summary"""
        tests = [
            ("User List Pagination", "/api/v1/admin/users", self.test_user_list_pagination),
            ("User Search", "/api/v1/admin/users", self.test_user_search),
            ("User Get By ID", "/api/v1/admin/users/{id}", self.test_user_get_by_id),
            ("User Update", "/api/v1/admin/users/{id}", self.test_user_update),
            ("User Toggle Active", "/api/v1/admin/users/{id}/toggle-active", self.test_user_toggle_active),
            ("User Password Reset", "/api/v1/admin/users/{id}/reset-password", self.test_user_password_reset),
            ("User Self-Delete Protection", "/api/v1/admin/users/{id}", self.test_user_cannot_delete_self),
            ("Menu List All", "/api/v1/admin/menu", self.test_menu_list_all),
            ("Menu Create", "/api/v1/admin/menu", self.test_menu_create),
            ("Menu Update", "/api/v1/admin/menu/{id}", self.test_menu_update),
            ("Menu Toggle Availability", "/api/v1/admin/menu/{id}/toggle-availability", self.test_menu_toggle_availability),
            ("Menu Delete (Soft)", "/api/v1/admin/menu/{id}", self.test_menu_delete),
            ("Menu Validation", "/api/v1/admin/menu", self.test_menu_validation_required_fields),
            ("Coupon Create", "/api/v1/admin/coupons", self.test_coupon_create),
            ("Coupon Update", "/api/v1/admin/coupons/{id}", self.test_coupon_update),
            ("Coupon Delete", "/api/v1/admin/coupons/{id}", self.test_coupon_delete),
            ("Category List", "/api/v1/admin/categories", self.test_category_list),
            ("Category CRUD", "/api/v1/admin/categories", self.test_category_create_update_delete),
            ("Dashboard Stats", "/api/v1/admin/dashboard/stats", self.test_dashboard_stats),
            ("Dashboard Charts", "/api/v1/admin/dashboard/charts", self.test_dashboard_charts),
            ("Dashboard Top Items", "/api/v1/admin/dashboard/top-items", self.test_dashboard_top_items),
            ("Unauthorized Access Blocked", "/api/v1/admin/*", self.test_unauthorized_access_blocked),
            ("Invalid ID Rejected", "/api/v1/admin/users/invalid", self.test_invalid_id_format_rejected),
            ("Pagination Edge Cases", "/api/v1/admin/users", self.test_pagination_bounds),
        ]
        
        print(f"\n🚀 Starting Admin Control QA Test Suite")
        print(f"📍 Base URL: {self.client.base_url}")
        print(f"👤 Authenticated as: {ADMIN_EMAIL}")
        print("=" * 70)
        
        for name, endpoint, test_fn in tests:
            self._run(name, endpoint, test_fn)
            status = self.results[-1].status
            print(f"{status.value:12} {name:40} [{endpoint}]")
        
        return self._generate_report()
    
    def _generate_report(self) -> Dict[str, Any]:
        """Generate test execution report"""
        total = len(self.results)
        passed = sum(1 for r in self.results if r.status == TestStatus.PASS)
        failed = sum(1 for r in self.results if r.status == TestStatus.FAIL)
        errors = sum(1 for r in self.results if r.status == TestStatus.ERROR)
        skipped = sum(1 for r in self.results if r.status == TestStatus.SKIP)
        avg_duration = sum(r.duration_ms for r in self.results) / total if total > 0 else 0
        return {
            'total': total, 'passed': passed, 'failed': failed, 'errors': errors, 'skipped': skipped,
            'pass_rate': (passed / total * 100) if total > 0 else 0, 'avg_duration_ms': avg_duration,
            'results': [asdict(r) for r in self.results]
        }

# ============================================================================
# Main Entry Point
# ============================================================================
def main():
    parser = argparse.ArgumentParser(description='Admin Control QA Test Suite')
    parser.add_argument('--base-url', default=BASE_URL, help=f'API base URL (default: {BASE_URL})')
    parser.add_argument('--email', default=ADMIN_EMAIL, help='Admin email')
    parser.add_argument('--password', default=ADMIN_PASSWORD, help='Admin password')
    parser.add_argument('--json', action='store_true', help='Output results as JSON')
    args = parser.parse_args()
    
    try:
        client = AdminAPIClient(args.base_url, args.email, args.password)
        tests = AdminControlTests(client)
        report = tests.run_all()
        
        if args.json:
            print(json.dumps(report, indent=2))
        else:
            print("\n" + "=" * 70)
            print("📊 TEST SUMMARY")
            print("=" * 70)
            print(f"Total Tests:  {report['total']}")
            print(f"✅ Passed:    {report['passed']} ({report['pass_rate']:.1f}%)")
            print(f"❌ Failed:    {report['failed']}")
            print(f"💥 Errors:    {report['errors']}")
            print(f"⚠️  Skipped:   {report['skipped']}")
            print(f"⏱️  Avg Time:  {report['avg_duration_ms']:.0f}ms/test")
            
            if report['failed'] > 0 or report['errors'] > 0:
                print("\n🔍 Failed/Error Details:")
                for r in tests.results:
                    if r.status in [TestStatus.FAIL, TestStatus.ERROR]:
                        print(f"  • {r.name}: {r.message}")
            sys.exit(0 if report['failed'] == 0 and report['errors'] == 0 else 1)
    except Exception as e:
        print(f"💥 Test suite failed to initialize: {e}")
        sys.exit(2)

if __name__ == '__main__':
    main()
