# Disaster Recovery Plan
## SuperMalle Restaurant System

**Document Version:** 1.0  
**Last Updated:** May 5, 2026  
**Owner:** Operations Team  
**Review Date:** May 5, 2027

---

## 1. Executive Summary

This document outlines the disaster recovery procedures for the SuperMalle Restaurant System. It provides step-by-step instructions for recovering from various types of failures and ensuring business continuity.

### Recovery Objectives

- **RPO (Recovery Point Objective):** 1 hour (maximum data loss)
- **RTO (Recovery Time Objective):** 4 hours (maximum downtime)
- **Availability Target:** 99.9% (8.76 hours downtime per year)

---

## 2. System Architecture Overview

### Components

1. **Application Server**
   - Spring Boot application
   - Docker containerized
   - Auto-scaling enabled

2. **Database**
   - PostgreSQL 15
   - Primary-Replica configuration
   - Daily backups

3. **Cache**
   - Redis 7
   - Cluster configuration
   - Data persistence enabled

4. **Message Queue**
   - RabbitMQ 3.12
   - Cluster configuration
   - Queue mirroring

5. **Monitoring**
   - Prometheus metrics
   - Grafana dashboards
   - Alert notifications

---

## 3. Backup Strategy

### Database Backups

**Frequency:**
- Full backup: Daily at 2:00 AM UTC
- Incremental backup: Every 4 hours
- Transaction log backup: Every 15 minutes

**Retention:**
- Daily backups: 30 days
- Weekly backups: 12 weeks
- Monthly backups: 12 months

**Storage:**
- Local storage: 7 days
- Off-site storage: 30 days
- Cloud storage: 12 months

### Application Backups

**Frequency:**
- Configuration: Daily
- Logs: Weekly
- Static assets: Monthly

**Retention:**
- Configuration: 90 days
- Logs: 30 days
- Static assets: 12 months

---

## 4. Recovery Procedures

### 4.1 Database Recovery

#### Scenario 1: Database Corruption

**Symptoms:**
- Database errors in logs
- Application unable to connect
- Data inconsistencies

**Recovery Steps:**

1. **Identify the issue**
   ```bash
   # Check database logs
   tail -f /var/log/postgresql/postgresql-15-main.log
   
   # Check database status
   sudo systemctl status postgresql
   ```

2. **Stop the application**
   ```bash
   # Stop application services
   docker-compose stop app
   ```

3. **Restore from backup**
   ```bash
   # List available backups
   ls -lh /var/backups/supermalle/
   
   # Restore from latest backup
   ./scripts/restore-database.sh /var/backups/supermalle/supermalle_production_20260505_020000.sql.gz
   ```

4. **Verify the restore**
   ```bash
   # Check database integrity
   psql -U postgres -d supermalle -c "SELECT COUNT(*) FROM users;"
   psql -U postgres -d supermalle -c "SELECT COUNT(*) FROM orders;"
   ```

5. **Start the application**
   ```bash
   # Start application services
   docker-compose start app
   ```

6. **Monitor the application**
   ```bash
   # Check application logs
   docker-compose logs -f app
   
   # Check health status
   curl http://localhost:8080/actuator/health
   ```

#### Scenario 2: Database Server Failure

**Symptoms:**
- Database server unreachable
- All database connections failing
- Network connectivity issues

**Recovery Steps:**

1. **Verify the failure**
   ```bash
   # Check database connectivity
   pg_isready -h localhost -p 5432
   
   # Check server status
   ping db-server
   ```

2. **Promote replica to primary**
   ```bash
   # Connect to replica server
   ssh replica-server
   
   # Promote replica
   pg_ctl promote -D /var/lib/postgresql/data
   ```

3. **Update application configuration**
   ```bash
   # Update database host in environment
   export DB_HOST=replica-server
   
   # Restart application
   docker-compose restart app
   ```

4. **Rebuild failed primary**
   ```bash
   # Once primary is back online
   # Rebuild as replica
   pg_basebackup -h replica-server -D /var/lib/postgresql/data -P -U replicator
   ```

### 4.2 Application Recovery

#### Scenario 1: Application Server Failure

**Symptoms:**
- Application not responding
- High error rates
- Memory/CPU issues

**Recovery Steps:**

1. **Identify the issue**
   ```bash
   # Check application logs
   docker-compose logs app
   
   # Check resource usage
   docker stats app
   ```

2. **Restart the application**
   ```bash
   # Restart application container
   docker-compose restart app
   ```

3. **If restart fails, rebuild**
   ```bash
   # Stop and remove container
   docker-compose down app
   
   # Rebuild and start
   docker-compose up -d --build app
   ```

4. **Verify the recovery**
   ```bash
   # Check health status
   curl http://localhost:8080/actuator/health
   
   # Check application logs
   docker-compose logs -f app
   ```

#### Scenario 2: Complete System Failure

**Symptoms:**
- All services down
- Network unreachable
- Power failure

**Recovery Steps:**

1. **Assess the situation**
   - Identify the scope of the failure
   - Determine the root cause
   - Estimate recovery time

2. **Restore from off-site backup**
   ```bash
   # Download latest backup from cloud storage
   aws s3 cp s3://supermalle-backups/latest.sql.gz /tmp/
   
   # Restore database
   ./scripts/restore-database.sh /tmp/latest.sql.gz
   ```

3. **Rebuild infrastructure**
   ```bash
   # Use infrastructure as code
   terraform apply
   
   # Deploy application
   docker-compose up -d
   ```

4. **Verify the recovery**
   ```bash
   # Run health checks
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/actuator/health/readiness
   curl http://localhost:8080/actuator/health/liveness
   ```

### 4.3 Data Recovery

#### Scenario 1: Accidental Data Deletion

**Symptoms:**
- Missing data
- User reports of lost information
- Data inconsistencies

**Recovery Steps:**

1. **Identify the affected data**
   ```bash
   # Check audit logs
   psql -U postgres -d supermalle -c "SELECT * FROM audit_logs WHERE action = 'DELETE' ORDER BY timestamp DESC LIMIT 10;"
   ```

2. **Restore from backup**
   ```bash
   # Find backup before deletion
   ls -lh /var/backups/supermalle/
   
   # Restore to temporary database
   createdb supermalle_temp
   gunzip -c backup.sql.gz | psql supermalle_temp
   
   # Extract and restore affected data
   psql supermalle_temp -c "\copy (SELECT * FROM users WHERE id = 123) TO STDOUT" | psql supermalle
   ```

3. **Verify the restore**
   ```bash
   # Check restored data
   psql -U postgres -d supermalle -c "SELECT * FROM users WHERE id = 123;"
   ```

#### Scenario 2: Data Corruption

**Symptoms:**
- Data integrity errors
- Application errors
- Inconsistent data

**Recovery Steps:**

1. **Identify corrupted data**
   ```bash
   # Check database integrity
   psql -U postgres -d supermalle -c "SELECT * FROM pg_stat_all_tables WHERE n_dead_tup > 0;"
   ```

2. **Restore from backup**
   ```bash
   # Restore from last known good backup
   ./scripts/restore-database.sh /var/backups/supermalle/supermalle_production_20260505_020000.sql.gz
   ```

3. **Apply transaction logs**
   ```bash
   # Apply transaction logs up to corruption point
   # (This requires point-in-time recovery setup)
   ```

---

## 5. Communication Plan

### Internal Communication

**Incident Response Team:**
- DevOps Lead
- Database Administrator
- Application Developer
- Operations Manager

**Communication Channels:**
- Slack: #incidents
- Email: incidents@supermalle.com
- Phone: Emergency contact list

### External Communication

**Stakeholders:**
- Management
- Customers
- Partners

**Communication Channels:**
- Email: status@supermalle.com
- Status Page: https://status.supermalle.com
- Social Media: @SuperMalleStatus

### Communication Templates

#### Initial Incident Notification

**Subject:** URGENT: System Incident - [Severity]

**Body:**
```
We are currently experiencing a system incident.

Severity: [Critical/High/Medium/Low]
Impact: [Description of impact]
Started: [Timestamp]
Status: [Investigating/Mitigating/Resolving]

Updates will be provided as they become available.
```

#### Resolution Notification

**Subject:** RESOLVED: System Incident - [Incident ID]

**Body:**
```
The system incident has been resolved.

Incident ID: [ID]
Started: [Timestamp]
Resolved: [Timestamp]
Duration: [Duration]
Root Cause: [Description]
Resolution: [Description]
Prevention: [Description]

We apologize for any inconvenience this may have caused.
```

---

## 6. Testing and Maintenance

### Regular Testing

**Monthly:**
- Backup verification
- Restore procedure test
- Failover test

**Quarterly:**
- Full disaster recovery drill
- Communication plan test
- Documentation review

**Annually:**
- Complete disaster recovery simulation
- Third-party audit
- Plan update

### Maintenance Schedule

**Weekly:**
- Review backup logs
- Check backup storage
- Update contact information

**Monthly:**
- Test backup integrity
- Review recovery procedures
- Update documentation

**Quarterly:**
- Conduct disaster recovery drill
- Review and update plan
- Train team members

---

## 7. Contact Information

### Incident Response Team

| Role | Name | Email | Phone |
|------|------|-------|-------|
| DevOps Lead | John Doe | john@supermalle.com | +1-555-0101 |
| Database Administrator | Jane Smith | jane@supermalle.com | +1-555-0102 |
| Application Developer | Bob Johnson | bob@supermalle.com | +1-555-0103 |
| Operations Manager | Alice Brown | alice@supermalle.com | +1-555-0104 |

### External Contacts

| Service | Contact | Phone |
|---------|---------|-------|
| Cloud Provider | AWS Support | +1-800-555-1234 |
| Database Support | PostgreSQL Support | +1-800-555-5678 |
| Security Team | Security Team | +1-800-555-9012 |

---

## 8. Appendix

### A. Backup Scripts

**Location:** `/scripts/`

- `backup-database.sh` - Database backup script
- `restore-database.sh` - Database restore script
- `verify-backup.sh` - Backup verification script

### B. Monitoring Endpoints

**Health Checks:**
- `/actuator/health` - General health
- `/actuator/health/readiness` - Readiness probe
- `/actuator/health/liveness` - Liveness probe

**Metrics:**
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/metrics` - Application metrics

### C. Emergency Procedures

**System Shutdown:**
```bash
# Stop all services
docker-compose down

# Stop database
sudo systemctl stop postgresql
```

**System Startup:**
```bash
# Start database
sudo systemctl start postgresql

# Start all services
docker-compose up -d
```

**Emergency Access:**
```bash
# Access database directly
sudo -u postgres psql supermalle

# Access application logs
docker-compose logs app
```

---

**Document Status:** ✅ APPROVED  
**Next Review:** May 5, 2027  
**Version History:**
- v1.0 (May 5, 2026) - Initial version
