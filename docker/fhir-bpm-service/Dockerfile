# mvn clean package
# buildah bud -f docker/fhir-bpm-service/Dockerfile -t quay.io/redhat_naps_da/fhir-bpm-service:0.0.10 .
# podman push quay.io/redhat_naps_da/fhir-bpm-service:0.0.10

FROM registry.redhat.io/ubi8/openjdk-11:1.3-15

USER 1001

COPY target/pneumonia-patient-processing-pam-0.0.1.jar /deployments/app.jar
