#!/bin/bash

# Backup Verification Script
# SuperMalle Restaurant System
# Usage: ./verify-backup.sh <backup_file>

set -e

# Configuration
BACKUP_FILE=${1}
BACKUP_DIR="/var/backups/supermalle"
TEMP_DB="supermalle_verify_$$"

# Database configuration (load from environment)
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

# Check if backup file is provided
if [ -z "${BACKUP_FILE}" ]; then
    echo "Error: Backup file not provided"
    echo "Usage: ./verify-backup.sh <backup_file>"
    exit 1
fi

# Check if backup file exists
if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Error: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

echo "========================================"
echo "Backup Verification Started"
echo "Backup file: ${BACKUP_FILE}"
echo "========================================"

# Step 1: Verify file exists and is readable
echo "Step 1: Verifying file exists and is readable..."
if [ -r "${BACKUP_FILE}" ]; then
    FILE_SIZE=$(du -h ${BACKUP_FILE} | cut -f1)
    echo "✓ File exists and is readable (${FILE_SIZE})"
else
    echo "✗ File is not readable"
    exit 1
fi

# Step 2: Verify checksum
echo "Step 2: Verifying checksum..."
if [ -f "${BACKUP_FILE}.md5" ]; then
    EXPECTED_CHECKSUM=$(cat ${BACKUP_FILE}.md5)
    ACTUAL_CHECKSUM=$(md5sum ${BACKUP_FILE} | cut -d' ' -f1)
    
    if [ "${EXPECTED_CHECKSUM}" == "${ACTUAL_CHECKSUM}" ]; then
        echo "✓ Checksum verified (${ACTUAL_CHECKSUM})"
    else
        echo "✗ Checksum verification failed!"
        echo "Expected: ${EXPECTED_CHECKSUM}"
        echo "Actual: ${ACTUAL_CHECKSUM}"
        exit 1
    fi
else
    echo "⚠ Checksum file not found, skipping checksum verification"
fi

# Step 3: Verify file is a valid gzip file
echo "Step 3: Verifying file format..."
if gzip -t ${BACKUP_FILE} 2>/dev/null; then
    echo "✓ File is a valid gzip file"
else
    echo "✗ File is not a valid gzip file"
    exit 1
fi

# Step 4: Extract and verify SQL content
echo "Step 4: Verifying SQL content..."
TEMP_SQL=$(mktemp)
gunzip -c ${BACKUP_FILE} > ${TEMP_SQL}

# Check for SQL keywords
if grep -q "CREATE TABLE" ${TEMP_SQL} && grep -q "INSERT INTO" ${TEMP_SQL}; then
    echo "✓ File contains valid SQL content"
else
    echo "✗ File does not contain expected SQL content"
    rm -f ${TEMP_SQL}
    exit 1
fi

# Count tables
TABLE_COUNT=$(grep -c "CREATE TABLE" ${TEMP_SQL})
echo "  Tables found: ${TABLE_COUNT}"

# Count records
RECORD_COUNT=$(grep -c "INSERT INTO" ${TEMP_SQL})
echo "  Records found: ${RECORD_COUNT}"

rm -f ${TEMP_SQL}

# Step 5: Test restore to temporary database
echo "Step 5: Testing restore to temporary database..."
PGPASSWORD=${DB_PASSWORD} createdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} ${TEMP_DB}

if [ $? -eq 0 ]; then
    echo "✓ Temporary database created"
else
    echo "✗ Failed to create temporary database"
    exit 1
fi

gunzip -c ${BACKUP_FILE} | PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${TEMP_DB} > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✓ Restore test successful"
else
    echo "✗ Restore test failed"
    PGPASSWORD=${DB_PASSWORD} dropdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -if ${TEMP_DB}
    exit 1
fi

# Step 6: Verify database structure
echo "Step 6: Verifying database structure..."

# Check for expected tables
EXPECTED_TABLES=("users" "orders" "menu_items" "categories" "cart_items" "inventory" "loyalty_program" "audit_logs" "feature_flags")
MISSING_TABLES=()

for table in "${EXPECTED_TABLES[@]}"; do
    TABLE_EXISTS=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${TEMP_DB} -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '${table}')")
    
    if [ "${TABLE_EXISTS}" == "t" ]; then
        echo "  ✓ Table '${table}' exists"
    else
        echo "  ✗ Table '${table}' missing"
        MISSING_TABLES+=("${table}")
    fi
done

if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
    echo "✗ Missing tables: ${MISSING_TABLES[*]}"
    PGPASSWORD=${DB_PASSWORD} dropdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -if ${TEMP_DB}
    exit 1
fi

# Step 7: Verify data integrity
echo "Step 7: Verifying data integrity..."

# Check for data in key tables
for table in "users" "orders" "menu_items"; do
    COUNT=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${TEMP_DB} -t -c "SELECT COUNT(*) FROM ${table}")
    echo "  ${table}: ${COUNT} records"
done

# Step 8: Clean up temporary database
echo "Step 8: Cleaning up temporary database..."
PGPASSWORD=${DB_PASSWORD} dropdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -if ${TEMP_DB}

if [ $? -eq 0 ]; then
    echo "✓ Temporary database cleaned up"
else
    echo "⚠ Failed to clean up temporary database"
fi

echo "========================================"
echo "Backup Verification Completed"
echo "Status: SUCCESS"
echo "========================================"

exit 0
