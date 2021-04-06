FROM quay.io/btison/ubi8-java8-mvn

USER root

COPY target/repository /m2/repository

RUN chown -R 1001:0 /m2/repository \
  && chmod -R 776 /m2/repository

USER 1001

COPY target/process-service-1.1.0-SNAPSHOT.jar /deployments/app.jar
