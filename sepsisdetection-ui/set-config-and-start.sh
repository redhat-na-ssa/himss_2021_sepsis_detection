#!/bin/sh

# This script updates env.js with deployment environment variables. Allows keycloak off/on and changing mapbox tokens

env_path=assets/js/env.js

function doReplace () {
  echo "set-config.sh:  Replacing $1  $2";
  sed -i "s/$1.*;/$1 = \'$2\';/g" $env_path
}

if [[ x$KIE_SERVER_URL != "x"  ]]; then
  doReplace "KIE_SERVER_URL" $KIE_SERVER_URL;
else
  echo "No env var:  KIE_SERVER_URL"
fi

if [[ x$KIE_SERVER_USERID != "x" ]]; then
  doReplace "KIE_SERVER_USERID" $KIE_SERVER_USERID;
else
  echo "No env var:  KIE_SERVER_USERID"
fi
if [[ x$KIE_SERVER_PASSWORD != "x" ]]; then
  doReplace "KIE_SERVER_PASSWORD" $KIE_SERVER_PASSWORD;
else
  echo "No env var:  KIE_SERVER_PASSWORD"
fi
if [[ x$DM_CONTAINER_ALIAS != "x" ]]; then
  doReplace "DM_CONTAINER_ALIAS" $DM_CONTAINER_ALIAS;
else
  echo "No env var:  DM_CONTAINER_ALIAS"
fi
if [[ x$PAM_CONTAINER_ALIAS != "x" ]]; then
  doReplace "PAM_CONTAINER_ALIAS" $PAM_CONTAINER_ALIAS;
else
  echo "No env var:  PAM_CONTAINER_ALIAS"
fi
if [[ x$PROCESS_ID != "x" ]]; then
  doReplace "PROCESS_ID" $PROCESS_ID;
else
  echo "No env var:  PROCESS_ID"
fi
if [[ x$FHIR_SERVER_URL != "x" ]]; then
  doReplace "FHIR_SERVER_URL" $FHIR_SERVER_URL;
else
  echo "No env var:  FHIR_SERVER_URL"
fi
if [[ x$PATIENT_VIEWER_URL != "x" ]]; then
  doReplace "PATIENT_VIEWER_URL" $PATIENT_VIEWER_URL;
else
  echo "No env var:  PATIENT_VIEWER_URL"
fi
if [[ x$IS_OPENSHIFT != "x" ]]; then
  doReplace "IS_OPENSHIFT" $IS_OPENSHIFT
else
  echo "No env var:  IS_OPENSHIFT"
fi

# Start nginx
nginx -g 'daemon off;'
