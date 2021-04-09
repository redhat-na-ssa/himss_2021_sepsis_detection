1. Start kafka (using community strimzi containers)
   `````
   $ podman-compose -f etc/docker-compose.yaml up -d
   `````

2. Start RH-PAM postgresql
   `````
   $ podman start postgresql
   `````

3. Clone, build and deploy pneumonia-patient-processing-kjar
   `````
   $ git clone https://github.com/redhat-naps-da/pneumonia-patient-processing-kjar.git
   $ cd pneumonia-patient-processing-kjar
   $ mvn clean install
   $ mvn deploy \
        -DaltDeploymentRepository="nexus::default::http://admin:admin123@$NEXUS_ROUTE/repository/redhat-naps/"
   `````

4. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -jar target/pneumonia-patient-processing-pam-0.0.1.jar
   `````

## Test

1. Health Check Report
   `````
   $ curl -X GET -H 'Content-type:application/json' localhost:8080/rest/server/healthcheck?report=true
   `````

2. List containers
   `````
   $ curl -X GET http://localhost:8080/rest/server/containers
   `````


3. Start a business process
   `````
   $ curl -X POST localhost:8080/fhir/processes/sendSampleCloudEvent/azra12350
   `````

4. List cases
   `````
   $ curl -X GET -H 'Content-type:application/json' localhost:8080/rest/server/queries/cases/
   `````

4. List process definitions
   `````
   $ curl -X GET -H 'Content-type:application/json' localhost:8080/rest/server/containers/*/processes/
   `````
