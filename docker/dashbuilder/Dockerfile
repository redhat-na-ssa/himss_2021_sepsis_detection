# Build: buildah bud -f docker/dashbuilder/Dockerfile -t quay.io/redhat_naps_da/dashbuilder-rhpam-db:0.0.2 docker/dashbuilder
# Push:  podman push  quay.io/redhat_naps_da/dashbuilder-rhpam-db:0.0.2

# https://github.com/Emergency-Response-Demo/container-images/tree/master/ubi8-java11-mvn
FROM quay.io/emergencyresponsedemo/ubi8-jdk-11-mvn:0.0.3

USER 0

# Add the following dashbuilder runtime to the same directory where this Dockerfile resides:
COPY --chown=185 dashbuilder-runtime-app-*.jar /deployments/app.jar

# Add the following dashboard zip to the same directory where this Dockerfile resides:
#  https://github.com/jesuino/dashbuilder-dashboards/raw/main/jBPM%20DB%20Dashboard/jbpm_reports.zip
#
# NOTE:  At runtime, set the following JVM system property:  -Ddashbuilder.import.base.dir=/opt/dashbuilder
RUN mkdir /opt/dashbuilder && chown -R 185 /opt/dashbuilder
COPY --chown=185 jbpm_reports.zip /opt/dashbuilder/

USER 185

