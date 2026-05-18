# Grafana Dashboard Configuration
# SuperMalle Restaurant System

# This directory contains Grafana dashboard configurations
# Dashboards are automatically loaded by Grafana on startup

# Dashboard: supermalle-dashboard.json
# - JVM Heap Memory Usage
# - JVM Thread Count
# - HTTP Request Rate
# - HTTP Response Time
# - Database Connection Pool
# - Circuit Breaker States

# To import dashboards:
# 1. Copy dashboard JSON files to this directory
# 2. Restart Grafana or use the Grafana UI to import
# 3. Dashboards will be available in the Grafana dashboard list

# Dashboard provisioning is configured in docker-compose.yml
# Volume mount: ./docker/grafana/provisioning:/etc/grafana/provisioning
