# mvn clean package
# buildah bud -f docker/fhir-bpm-service/Dockerfile -t quay.io/redhat_naps_da/fhir-bpm-service:0.0.12 .
# podman push quay.io/redhat_naps_da/fhir-bpm-service:0.0.12

# https://github.com/Emergency-Response-Demo/container-images/tree/master/ubi8-java11-mvn
FROM quay.io/emergencyresponsedemo/ubi8-jdk-11-mvn:0.0.2

RUN chown -R 1001:0 /m2/repository \
  && chmod -R 776 /m2/repository

USER 1001

COPY target/*.jar /deployments/app.jar
