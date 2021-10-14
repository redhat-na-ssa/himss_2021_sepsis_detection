export interface BackendSettings {
    keycloakUrl: string,
    ssoRealm: string,
    ssoClient: string,
    kieserverUrl : string;
    dmcontainerAlias? : string;
    username? : string;
    password? : string;
    picontainerAlias?: string;
    processId? : string;
    isOpenShift? : boolean;
    fhirserverURL? : string;
    patientViewerURL? : string;
}
