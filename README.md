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

3. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -jar target/pneumonia-patient-processing-pam-0.0.1.jar
   `````
