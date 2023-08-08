// blank values are replaced at runtime by the set-config.js node script
(function(window) {
  window._env = window._env || {};

  window._env.KEYCLOAK_URL = "http://sso.local:4080";
  window._env.SSO_REALM = "kc-demo";
  window._env.SSO_CLIENT = "sepsisdetection";
  window._env.KIE_SERVER_URL = "http://rht:9080";
  window._env.KIE_SERVER_USERID = 'kieserver';
  window._env.KIE_SERVER_PASSWORD = 'kieserver';
  window._env.DM_CONTAINER_ALIAS = '';
  window._env.PAM_CONTAINER_ALIAS = 'sepsisdetection-kjar-1.0.0';
  window._env.PROCESS_ID = 'sepsisdetection';
  window._env.FHIR_SERVER_URL = 'http://rht:8000/';
  window._env.FHIR_SSE_STREAMING_URL = 'http://rht:4199';
  window._env.PATIENT_VIEWER_URL = 'https://demo.healthflow.io/profile/5e3ad7fe-24fa-4dc2-8be5-25554a8efb2c';
  window._env.IS_OPENSHIFT = 'false';
  window._env.SMILE_CDR_USERNAME = 'admin';
  window._env.SMILE_CDR_PASSWORD = 'password';
})(this);
