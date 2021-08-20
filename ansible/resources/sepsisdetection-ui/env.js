// blank values are replaced at runtime by the set-config.js node script
(function(window) {
  window._env = window._env || {};

  window._env.KIE_SERVER_URL = "https://{{ sepsisdetection_rhpam_hostname }}";
  window._env.KIE_SERVER_USERID = "kieserver";
  window._env.KIE_SERVER_PASSWORD = "{{ sepsisdetection_rhpam_api_passwd }}";
  window._env.DM_CONTAINER_ALIAS = "";
  window._env.PAM_CONTAINER_ALIAS = "{{ sepsisdetection_rhpam_deployment_name }}";
  window._env.PROCESS_ID = "{{ sepsisdetection_rhpam_process_id }}";
  window._env.FHIR_SERVER_URL = "https://{{ fhir_server_application_hostname }}";
  window._env.PATIENT_VIEWER_URL = "{{ patient_viewer_url }}";
  window._env.IS_OPENSHIFT = "true";
})(this);
