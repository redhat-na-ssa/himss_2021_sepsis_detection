mvn clean package \
    -DskipTests \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.tag=0.0.1

podman push quay.io/redhat_naps_da/sepsisdetection-ui-sse:0.0.1
