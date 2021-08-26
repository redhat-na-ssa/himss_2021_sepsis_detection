// blank values are replaced at runtime by the set-config.js node script
(function(window) {
  window._env = window._env || {};

  window._env.KIE_SERVER_URL = "http://localhost:9080";
  window._env.KIE_SERVER_USERID = 'kieserver';
  window._env.KIE_SERVER_PASSWORD = 'kieserver';
  window._env.DM_CONTAINER_ALIAS = '';
  window._env.PAM_CONTAINER_ALIAS = 'sepsisdetection-kjar-1.0.0';
  window._env.PROCESS_ID = 'sepsisdetection';
  window._env.FHIR_SERVER_URL = 'http://localhost:8080';
  window._env.PATIENT_VIEWER_URL = 'https://my.healthflow.io';
  window._env.IS_OPENSHIFT = 'false';
})(this);
