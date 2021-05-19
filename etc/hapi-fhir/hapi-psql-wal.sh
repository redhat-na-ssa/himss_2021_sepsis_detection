echo "Enabling for Debezium"
psql -c "ALTER USER ${POSTGRESQL_USER} with Replication;"

# Debezium needs ability to execute the following (which requires superuser) when using pgoutput plugin: "CREATE PUBLICATION dbz_publication FOR ALL TABLES;"
#   https://debezium.io/documentation/reference/connectors/postgresql.html#postgresql-replication-user-privileges
psql -c "ALTER USER ${POSTGRESQL_USER} with superuser;"
