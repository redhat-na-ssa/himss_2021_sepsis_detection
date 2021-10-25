$ kafka-topics.sh --bootstrap-server localhost:9094  --list

$ kafka-console-producer.sh \
        --broker-list localhost:9094 \
        --property parse.key=true \
        --property key.separator=":" \                                                            
        --topic risk-assessment-command

$ ./mvnw clean package \
    -DskipTests \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.tag=0.0.4

$ podman push quay.io/redhat_naps_da/sepsisdetection-risk:0.0.4
