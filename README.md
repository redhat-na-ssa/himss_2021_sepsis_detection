1. Start kafka (using community strimzi containers)
   `````
   $ podman-compose -f etc/docker-compose.yaml up -d
   `````

2. Execute rhpam sql scripts on database:
   `````
   $ podman exec -it etc_db_1 /bin/bash
   $ cd /opt/sql \
     && psql -U rhpam -d rhpam -f postgresql-jbpm-schema.sql \
     && psql -U rhpam -d rhpam -f task_assigning_tables_postgresql.sql \
     && psql -U rhpam -d rhpam -f postgresql-jbpm-lo-trigger-clob.sql \
     && psql -U rhpam -d rhpam -f quartz_tables_postgres.sql \
     && psql rhpam -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO rhpam;" \
     && psql rhpam -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO rhpam;"
   `````

   TO-DO:  Create a RH-PAM image seeded with the above tables as per:  https://github.com/sclorg/postgresql-container/tree/generated/12#extending-image

2. Tear down pod:
   `````
   $ podman-compose -f etc/docker-compose.yaml down
   `````

3. Clone, build and deploy pneumonia-patient-processing-kjar
   `````
   $ git clone https://github.com/redhat-naps-da/pneumonia-patient-processing-kjar.git
   $ cd pneumonia-patient-processing-kjar
   $ mvn clean install
   `````

4. Optional :  Push to nexus:
   `````
   $ mvn deploy \
        -DaltDeploymentRepository="nexus::default::http://admin:admin123@$NEXUS_ROUTE/repository/redhat-naps/" 
   `````

5. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -Dorg.kie.server.repo=/tmp \
              -jar target/pneumonia-patient-processing-pam-0.0.1.jar 
   `````

5. Optional:  Run from container
   `````
   $ podman run --rm \
            --name fhir-bpm-service \
            --publish 8080:8080 \
            -v ./config:/deployments/config \
            -m 512m \
            -e JAVA_MAX_MEM_RATIO=60 \
            -e JAVA_INITIAL_MEM_RATIO=0 \
            -e GC_MAX_METASPACE_SIZE=500 \
            quay.io/redhat_naps_da/fhir-bpm-service:0.0.4
   `````

## Test

1. Set environment variables to support testing:
   `````
   $ export KJAR_VERSION=1.0.3
   $ export KIE_SERVER_CONTAINER_NAME=fhir-bpm-service
   `````

2. Health Check Report
   `````
   $ curl -u "user:user" -H 'Accept:application/json' localhost:8080/rest/server/healthcheck?report=true
   `````

3. View swagger
   `````
   $ curl -v -u "user:user" localhost:8080/rest/swagger.json | jq .
   `````

4. Create a container in kie-server:
   `````
   $ sed "s/{KIE_SERVER_CONTAINER_NAME}/$KIE_SERVER_CONTAINER_NAME/g" etc/kie_container.json \
     | sed "s/{KJAR_VERSION}/$KJAR_VERSION/g" \
     > /tmp/kie_container.json && \
     curl -u "user:user" -X PUT -H 'Content-type:application/json' localhost:8080/rest/server/containers/$KIE_SERVER_CONTAINER_NAME-$KJAR_VERSION -d '@/tmp/kie_container.json'
   `````

5. List containers
   `````
   $ curl -u "user:user" -X GET http://localhost:8080/rest/server/containers
   `````

6. Start a business process
   `````
   $ curl -X POST localhost:8080/fhir/processes/sendSampleCloudEvent/azra12350
   `````

7. List cases in JSON representation:
   `````
   $ curl -u "user:user" -X GET -H 'Accept:application/json' localhost:8080/rest/server/queries/cases/
   `````

8. List process definitions in JSON representation:
   `````
   $ curl -u "user:user" -X GET -H 'Accept:application/json' localhost:8080/rest/server/containers/$KIE_SERVER_CONTAINER_NAME-$KJAR_VERSION/processes/
   `````
