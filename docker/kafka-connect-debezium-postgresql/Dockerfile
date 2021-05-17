FROM registry.redhat.io/amq7/amq-streams-kafka-26-rhel7:1.7.0

ENV DEBEZIUM_VERSION=1.5.0.Final

USER root:root

COPY contrib/debezium-connector-postgres /opt/kafka/plugins/debezium-connector-postgres

USER 1001
