

echo "creating RHPAM database"

psql -d $POSTGRESQL_DATABASE < ${SCRIPT_DIR}/postgresql-jbpm-schema.sql
psql -d $POSTGRESQL_DATABASE < ${SCRIPT_DIR}/task_assigning_tables_postgresql.sql
psql -d $POSTGRESQL_DATABASE < ${SCRIPT_DIR}/quartz_tables_postgres.sql
psql -d $POSTGRESQL_DATABASE < ${SCRIPT_DIR}/postgresql-jbpm-lo-trigger-clob.sql

psql -d $POSTGRESQL_DATABASE -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${POSTGRESQL_USER};" 
psql -d $POSTGRESQL_DATABASE -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${POSTGRESQL_USER};"
