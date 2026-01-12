#!/usr/bin/env bash

# SAVE
/opt/ankey/backup.sh

# Set branch for kibana logging
if [ -n "${ANKEYIDM_BRANCH_LOG_TOKEN}" ]
then
  sed -i 's/BRANCH/'"${ANKEYIDM_BRANCH_LOG_TOKEN}"'/g' /opt/ankey/logger/logback.xml
else
  echo "No parameter ANKEYIDM_BRANCH_LOG_TOKEN found. "
fi

#Changing url in repo.jdbc
if [ -n "${ANKEYIDM_PG_URL}" ]
then
sed -i 's/localhost:5432/'"${ANKEYIDM_PG_URL}"'/g' /opt/ankey/conf/repo.jdbc.json
else
  echo "No parameter ANKEYIDM_PG_URL was found. "
fi

#Changing username in repo.jdbc
if [ -n "${ANKEYIDM_PG_USER}" ]
then
sed -i 's/"username" : "ankey"/"username" : "'"${ANKEYIDM_PG_USER}"'"/g' /opt/ankey/conf/repo.jdbc.json
sed -i 's/"user" : "ankey"/"user" : "'"${ANKEYIDM_PG_USER}"'"/g' /opt/ankey/conf/repo.jdbc.json
else
  echo "No parameter ANKEYIDM_PG_USER was found. "
fi


# Set Elastic host
if [ -n "${ANKEYIDM_ELASTIC_HOST}" ]
then
  sed -i "s/\"opensearch\"/\"${ANKEYIDM_ELASTIC_HOST}\"/g" /opt/ankey/conf/search.connection.json
else
  echo "No parameter ANKEYIDM_ELASTIC_HOST found. "
fi

# Set Elastic host
if [ -n "${ANKEYIDM_ELASTIC_HOST}" ]
then
  sed -i "s/\"localhost\"/\"${ANKEYIDM_ELASTIC_HOST}\"/g" /opt/ankey/conf/search.connection.json
else
  echo "No parameter ANKEYIDM_ELASTIC_HOST found. "
fi

# Set Elastic host
if [ -n "${ANKEYIDM_ELASTIC_PORT}" ]
then
  sed -i 's/9200/'"${ANKEYIDM_ELASTIC_PORT}"'/g' /opt/ankey/conf/search.connection.json
else
  echo "No parameter ANKEYIDM_ELASTIC_PORT found. "
fi

# Set Elastic index
if [ -n "${ANKEYIDM_ELASTIC_INDEX}" ]
then
  sed -i 's/ankey/ankey-'"${ANKEYIDM_ELASTIC_INDEX}"'/g' /opt/ankey/conf/search.settings.json
else
  echo "No parameter ANKEYIDM_ELASTIC_INDEX found. "
fi

# Set Kafka host
if [ -n "${ANKEYIDM_KAFKA_HOST}" ]
then
  sed -i 's/localhost:9092/'${ANKEYIDM_KAFKA_HOST}'/g' /opt/ankey/conf/kafka.configuration.json
else
  echo "No parameter ANKEYIDM_KAFKA_HOST found. "
fi

# Set Kafka index
if [ -n "${ANKEYIDM_KAFKA_INDEX}" ]
then
  sed -i 's/"prefix": "ankey"/"group.instance.id": "'"${ANKEYIDM_KAFKA_INDEX}"'",\n    "group.id": "'"${ANKEYIDM_KAFKA_INDEX}"'",\n    "prefix": "'"${ANKEYIDM_KAFKA_INDEX}"'"/g' /opt/ankey/conf/kafka.configuration.json
else
  echo "No parameter ANKEYIDM_KAFKA_INDEX found. "
fi


# Set max log file size
if [ -n "${ANKEYIDM_MAX_LOG_SIZE}" ]
then
  sed -i 's/java.util.logging.FileHandler.limit = 5242880/java.util.logging.FileHandler.limit = '"${ANKEYIDM_MAX_LOG_SIZE}"'/g' /opt/ankey/logger/logback.xml
else
  echo "No parameter ANKEYIDM_MAX_LOG_SIZE found. "
fi

# Set Report host
if [ -n "${ANKEYIDM_REPORT_HOST}" ]
then
  sed -i 's/"url".*/"url":"'"${ANKEYIDM_REPORT_HOST}\/report\/v1"'"/g' /opt/ankey/conf/report.microservices.json
  sed -i 's/"enabled": false/"enabled": true/g' /opt/ankey/conf/report.microservices.json
else
  echo "No parameter ANKEYIDM_REPORT_HOST found. "
fi

# Set Bpmn host
if [ -n "${ANKEYIDM_BPMN_HOST}" ]
then
  sed -i 's/"url".*/"url":"'"${ANKEYIDM_BPMN_HOST}\/bpmn\/v1"'"/g' /opt/ankey/conf/bpmn.microservices.json
  sed -i 's/"enabled": false/"enabled": true/g' /opt/ankey/conf/bpmn.microservices.json
else
  echo "No parameter ANKEYIDM_BPMN_HOST found. "
fi

#Setting up connectors service
if [ -n "${ANKEYIDM_CONNECTORS_SERVICE_HOST}" ]
then
  chmod +x /opt/connectors_service.sh
  /opt/connectors_service.sh ${ANKEYIDM_CONNECTORS_SERVICE_HOST} &
else
  echo "No parameter ANKEYIDM_CONNECTORS_SERVICE_HOST found. "
fi


/opt/wait-for-it.sh $1
/opt/ankey/startup.sh jpda

