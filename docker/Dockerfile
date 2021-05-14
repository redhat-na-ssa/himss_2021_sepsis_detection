# mvn clean package
# buildah bud -f docker/Dockerfile -t quay.io/redhat_naps_da/fhir-bpm-service:0.0.1 .
# podman push quay.io/redhat_naps_da/fhir-bpm-service:0.0.1

FROM quay.io/emergencyresponsedemo/ubi8-jdk-11-mvn:0.0.2

USER 1001

COPY target/pneumonia-patient-processing-pam-0.0.1.jar /deployments/app.jar
