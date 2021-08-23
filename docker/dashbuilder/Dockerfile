# Build: buildah bud -f docker/dashbuilder/Dockerfile -t quay.io/redhat_naps_da/dashbuilder-rhpam-db:0.0.1 docker/dashbuilder
# Push:  podman push  quay.io/redhat_naps_da/dashbuilder-rhpam-db:0.0.1

# https://github.com/Emergency-Response-Demo/container-images/tree/master/ubi8-java11
FROM quay.io/emergencyresponsedemo/ubi8-java11:8.3

# Add the following dashbuilder runtime to the same directory where this Dockerfile resides:
COPY --chown=1001 dashbuilder-runtime-app-*.jar /deployments/app.jar

# Add the following dashboard zip to the same directory where this Dockerfile resides:
#  https://github.com/jesuino/dashbuilder-dashboards/raw/main/jBPM%20DB%20Dashboard/jbpm_reports.zip
#
# NOTE:  At runtime, set the following JVM system property:  -Ddashbuilder.import.base.dir=/opt/dashbuilder
USER root
RUN mkdir /opt/dashbuilder && chown -R 1001 /opt/dashbuilder
USER 1001
COPY --chown=1001 jbpm_reports.zip /opt/dashbuilder/

