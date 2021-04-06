1. Start kafka (using community strimzi containers)
   `````
   $ podman-compose -f etc/docker-compose.yaml up -d
   `````

2. Start RH-PAM postgresql
   `````
   $ podman start postgresql
   `````

3. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -jar target/pneumonia-patient-processing-pam-0.0.1.jar
   `````
