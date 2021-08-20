'use strict';

/* This script updates env.js with deployment environment variables. Allows keycloak off/on and changing mapbox tokens */

const replace = require('replace');

doReplace('KIE_SERVER_URL', process.env.KIE_SERVER_URL);
doReplace('KIE_SERVER_USERID', process.env.KIE_SERVER_USERID);
doReplace('KIE_SERVER_PASSWORD', process.env.KIE_SERVER_PASSWORD);
doReplace('DM_CONTAINER_ALIAS', process.env.DM_CONTAINER_ALIAS);
doReplace('PAM_CONTAINER_ALIAS', process.env.PAM_CONTAINER_ALIAS);
doReplace('PROCESS_ID', process.env.PROCESS_ID);
doReplace('FHIR_SERVER_URL', process.env.FHIR_SERVER_URL);
doReplace('PATIENT_VIEWER_URL', process.env.PATIENT_VIEWER_URL);
doReplace('IS_OPENSHIFT', process.env.IS_OPENSHIFT);

function doReplace(key, value) {
  const regex = key + ' = \'.*\'';
  const replacement = key + ' = \''+ value +'\'';

  replace({
    regex: regex,
    replacement: replacement,
    paths: [`${__dirname}/dist/sepsis-ui/assets/js/env.js`]
  });
}
