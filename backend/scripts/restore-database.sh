#!/bin/bash

# Database Restore Script
# SuperMalle Restaurant System
# Usage: ./restore-database.sh <backup_file> [environment]

set -e

# Configuration
ENVIRONMENT=${2:-production}
BACKUP_FILE=${1}
BACKUP_DIR="/var/backups/supermalle"

# Database configuration (load from environment)
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-supermalle}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

# Log file
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="${BACKUP_DIR}/restore_${TIMESTAMP}.log"

# Check if backup file is provided
if [ -z "${BACKUP_FILE}" ]; then
    echo "Error: Backup file not provided"
    echo "Usage: ./restore-database.sh <backup_file> [environment]"
    exit 1
fi

# Check if backup file exists
if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Error: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

# Verify checksum if available
if [ -f "${BACKUP_FILE}.md5" ]; then
    echo "Verifying backup checksum..." | tee -a ${LOG_FILE}
    EXPECTED_CHECKSUM=$(cat ${BACKUP_FILE}.md5)
    ACTUAL_CHECKSUM=$(md5sum ${BACKUP_FILE} | cut -d' ' -f1)
    
    if [ "${EXPECTED_CHECKSUM}" != "${ACTUAL_CHECKSUM}" ]; then
        echo "Error: Checksum verification failed!" | tee -a ${LOG_FILE}
        echo "Expected: ${EXPECTED_CHECKSUM}" | tee -a ${LOG_FILE}
        echo "Actual: ${ACTUAL_CHECKSUM}" | tee -a ${LOG_FILE}
        exit 1
    fi
    
    echo "Checksum verified successfully." | tee -a ${LOG_FILE}
fi

echo "========================================" | tee -a ${LOG_FILE}
echo "Database Restore Started" | tee -a ${LOG_FILE}
echo "Environment: ${ENVIRONMENT}" | tee -a ${LOG_FILE}
echo "Backup file: ${BACKUP_FILE}" | tee -a ${LOG_FILE}
echo "Timestamp: ${TIMESTAMP}" | tee -a ${LOG_FILE}
echo "========================================" | tee -a ${LOG_FILE}

# Confirm restore
echo "WARNING: This will replace the current database!" | tee -a ${LOG_FILE}
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "${confirm}" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

# Create pre-restore backup
echo "Creating pre-restore backup..." | tee -a ${LOG_FILE}
PRE_RESTORE_BACKUP="supermalle_${ENVIRONMENT}_pre_restore_${TIMESTAMP}.sql.gz"
PGPASSWORD=${DB_PASSWORD} pg_dump -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} | gzip > ${BACKUP_DIR}/${PRE_RESTORE_BACKUP}

if [ $? -eq 0 ]; then
    echo "Pre-restore backup created: ${PRE_RESTORE_BACKUP}" | tee -a ${LOG_FILE}
else
    echo "Warning: Pre-restore backup failed!" | tee -a ${LOG_FILE}
    read -p "Continue with restore anyway? (yes/no): " continue_restore
    if [ "${continue_restore}" != "yes" ]; then
        echo "Restore cancelled."
        exit 0
    fi
fi

# Drop existing database
echo "Dropping existing database..." | tee -a ${LOG_FILE}
PGPASSWORD=${DB_PASSWORD} dropdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -if ${DB_NAME}

# Create new database
echo "Creating new database..." | tee -a ${LOG_FILE}
PGPASSWORD=${DB_PASSWORD} createdb -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} ${DB_NAME}

# Restore database
echo "Restoring database from backup..." | tee -a ${LOG_FILE}
gunzip -c ${BACKUP_FILE} | PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}

# Check if restore was successful
if [ $? -eq 0 ]; then
    echo "Restore completed successfully!" | tee -a ${LOG_FILE}
    
    # Verify database
    echo "Verifying database integrity..." | tee -a ${LOG_FILE}
    TABLE_COUNT=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'")
    echo "Tables restored: ${TABLE_COUNT}" | tee -a ${LOG_FILE}
    
    echo "========================================" | tee -a ${LOG_FILE}
    echo "Database Restore Completed" | tee -a ${LOG_FILE}
    echo "========================================" | tee -a ${LOG_FILE}
    
    # Send notification (optional)
    # curl -X POST https://api.supermalle.com/notifications/restore \
    #   -H "Content-Type: application/json" \
    #   -d "{\"status\": \"success\", \"environment\": \"${ENVIRONMENT}\", \"backup_file\": \"${BACKUP_FILE}\"}"
    
    exit 0
else
    echo "Restore failed!" | tee -a ${LOG_FILE}
    echo "Pre-restore backup available: ${PRE_RESTORE_BACKUP}" | tee -a ${LOG_FILE}
    
    # Send notification (optional)
    # curl -X POST https://api.supermalle.com/notifications/restore \
    #   -H "Content-Type: application/json" \
    #   -d "{\"status\": \"failed\", \"environment\": \"${ENVIRONMENT}\", \"backup_file\": \"${BACKUP_FILE}\"}"
    
    exit 1
fi
