#! /bin/bash

export PGPASSWORD=$POSTGRESQL_PASSWORD

SCRIPT_DIR=''
pushd "$(dirname "$(readlink -f "$BASH_SOURCE")")" > /dev/null && {
    SCRIPT_DIR="$PWD"
    popd > /dev/null
}   

echo "creating FHIR Server database"

psql -h $POSTGRESQL_SERVICE -d $POSTGRESQL_DATABASE -U $POSTGRESQL_USER -w -c "grant all privileges on database ${POSTGRESQL_DATABASE} to ${POSTGRESQL_USER};"

echo "Enabling for Debezium"
export PGPASSWORD=$POSTGRESQL_ADMIN_PASSWORD
psql -h $POSTGRESQL_SERVICE -c "ALTER USER ${POSTGRESQL_USER} with Replication;"

# Debezium needs ability to execute the following (which requires superuser) when using pgoutput plugin: "CREATE PUBLICATION dbz_publication FOR ALL TABLES;"
#   https://debezium.io/documentation/reference/connectors/postgresql.html#postgresql-replication-user-privileges
psql -h $POSTGRESQL_SERVICE  -c "ALTER USER ${POSTGRESQL_USER} with superuser;"

