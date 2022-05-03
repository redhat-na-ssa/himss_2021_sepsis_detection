// blank values are replaced at runtime by the set-config.js node script
(function(window) {
  window._env = window._env || {};

  window._env.KEYCLOAK_URL = "{{ sso_url }}/auth";
  window._env.SSO_REALM = "{{ sso_realm_id }}";
  window._env.SSO_CLIENT = "{{ sso_clientId }}";
  window._env.KIE_SERVER_URL = "https://{{ sepsisdetection_rhpam_hostname }}";
  window._env.KIE_SERVER_USERID = "kieserver";
  window._env.KIE_SERVER_PASSWORD = "{{ sepsisdetection_rhpam_api_passwd }}";
  window._env.DM_CONTAINER_ALIAS = "";
  window._env.PAM_CONTAINER_ALIAS = "{{ sepsisdetection_rhpam_deployment_name }}-{{ sepsisdetection_rhpam_deployment_version }}";
  window._env.PROCESS_ID = "{{ sepsisdetection_rhpam_process_id }}";

  // CORS is enabled only on the fhir context-path
  window._env.FHIR_SERVER_URL = "https://{{ fhir_server_application_hostname }}/fhir";

  window._env.FHIR_SSE_STREAMING_URL = 'https://{{ sepsisdetection_sse_hostname }}';

  window._env.PATIENT_VIEWER_URL = "{{ patient_viewer_url }}";
  window._env.IS_OPENSHIFT = "true";
})(this);
