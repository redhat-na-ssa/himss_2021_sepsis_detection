FROM registry.redhat.io/amq7/amq-streams-kafka-31-rhel8:2.1.0-5.1661798502

ENV DEBEZIUM_VERSION=2.0.0.Beta2

USER root:root

COPY contrib/debezium-connector-postgres /opt/kafka/plugins/debezium-connector-postgres

USER 1001
