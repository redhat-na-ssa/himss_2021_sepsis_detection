mvn clean package \
    -DskipTests \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.tag=0.0.5

podman push quay.io/redhat_naps_da/sepsisdetection-sse:0.0.5
