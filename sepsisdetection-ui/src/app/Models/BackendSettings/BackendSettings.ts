export interface BackendSettings {
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
