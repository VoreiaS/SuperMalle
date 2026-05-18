#!/bin/bash

# Database Backup Script
# SuperMalle Restaurant System
# Usage: ./backup-database.sh [environment]

set -e

# Configuration
ENVIRONMENT=${1:-production}
BACKUP_DIR="/var/backups/supermalle"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="supermalle_${ENVIRONMENT}_${TIMESTAMP}.sql.gz"
RETENTION_DAYS=30

# Database configuration (load from environment)
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-supermalle}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

# Create backup directory
mkdir -p ${BACKUP_DIR}

# Log file
LOG_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.log"

echo "========================================" | tee -a ${LOG_FILE}
echo "Database Backup Started" | tee -a ${LOG_FILE}
echo "Environment: ${ENVIRONMENT}" | tee -a ${LOG_FILE}
echo "Timestamp: ${TIMESTAMP}" | tee -a ${LOG_FILE}
echo "========================================" | tee -a ${LOG_FILE}

# Perform backup
echo "Starting database backup..." | tee -a ${LOG_FILE}
PGPASSWORD=${DB_PASSWORD} pg_dump -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} | gzip > ${BACKUP_DIR}/${BACKUP_FILE}

# Check if backup was successful
if [ $? -eq 0 ]; then
    BACKUP_SIZE=$(du -h ${BACKUP_DIR}/${BACKUP_FILE} | cut -f1)
    echo "Backup completed successfully!" | tee -a ${LOG_FILE}
    echo "Backup file: ${BACKUP_DIR}/${BACKUP_FILE}" | tee -a ${LOG_FILE}
    echo "Backup size: ${BACKUP_SIZE}" | tee -a ${LOG_FILE}
    
    # Calculate checksum
    CHECKSUM=$(md5sum ${BACKUP_DIR}/${BACKUP_FILE} | cut -d' ' -f1)
    echo "Checksum: ${CHECKSUM}" | tee -a ${LOG_FILE}
    
    # Save checksum
    echo ${CHECKSUM} > ${BACKUP_DIR}/${BACKUP_FILE}.md5
    
    echo "Backup completed successfully!" | tee -a ${LOG_FILE}
else
    echo "Backup failed!" | tee -a ${LOG_FILE}
    exit 1
fi

# Clean up old backups
echo "Cleaning up old backups (older than ${RETENTION_DAYS} days)..." | tee -a ${LOG_FILE}
find ${BACKUP_DIR} -name "supermalle_${ENVIRONMENT}_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "supermalle_${ENVIRONMENT}_*.sql.gz.md5" -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "backup_*.log" -mtime +${RETENTION_DAYS} -delete

echo "Cleanup completed." | tee -a ${LOG_FILE}

# List current backups
echo "Current backups:" | tee -a ${LOG_FILE}
ls -lh ${BACKUP_DIR}/supermalle_${ENVIRONMENT}_*.sql.gz | tee -a ${LOG_FILE}

echo "========================================" | tee -a ${LOG_FILE}
echo "Database Backup Completed" | tee -a ${LOG_FILE}
echo "========================================" | tee -a ${LOG_FILE}

# Send notification (optional)
# curl -X POST https://api.supermalle.com/notifications/backup \
#   -H "Content-Type: application/json" \
#   -d "{\"status\": \"success\", \"environment\": \"${ENVIRONMENT}\", \"file\": \"${BACKUP_FILE}\"}"

exit 0
