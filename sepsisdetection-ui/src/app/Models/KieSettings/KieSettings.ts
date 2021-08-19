export interface KieSettings
{
    baseurl : string;
    dmcontainerAlias? : string;
    username? : string;
    password? : string;
    picontainerAlias?: string;
    processId? : string;
    isOpenShift? : boolean;
    fhirserverURL? : string;
    patientViewerURL? : string;
}