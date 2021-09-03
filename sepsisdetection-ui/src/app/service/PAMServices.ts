import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CookieService } from 'ngx-cookie';
import { KieSettings } from '../Models/KieSettings/KieSettings';
import { Part, ServiceRequest, ProcessInstanceData, Claim, Policy } from '../Models/Requests/Request';
import { RootObject, Command, Insert } from '../Models/Requests/Request';
import { Credentials } from '../Models/UserRole';
import { Applicant } from '../Models/Requests/Request';
import { Bundle } from '../component/Admin/Bundle';

@Injectable({ providedIn: 'root' })
export class PAMServices {

  private kieSettings: KieSettings;

  bundleData: string;

  constructor(private http: HttpClient, private cookieService: CookieService) {
    this.cookieService.putObject("test",{testcookie : "test"});
    this.kieSettings = <KieSettings>this.cookieService.getObject("himms")
    if (this.kieSettings === undefined) {
      this.kieSettings = {
        baseurl: window['_env'].KIE_SERVER_URL,
        dmcontainerAlias: window['_env'].DM_CONTAINER_ALIAS,
        picontainerAlias: window['_env'].PAM_CONTAINER_ALIAS,
        processId: window['_env'].PROCESS_ID,
        username: window['_env'].KIE_SERVER_USERID,
        password: window['_env'].KIE_SERVER_PASSWORD,
        fhirserverURL: window['_env'].FHIR_SERVER_URL,
        patientViewerURL: window['_env'].PATIENT_VIEWER_URL,
        isOpenShift: window['_env'].IS_OPENSHIFT
      };

      this.cookieService.putObject("himms", this.kieSettings);
    }

    if(localStorage.getItem("bundleData")) {
      this.bundleData = localStorage.getItem("bundleData");
    } else {
      this.bundleData = JSON.stringify(new Bundle().getConfiguration());
    }
    localStorage.setItem("bundleData",this.bundleData);
   }
 
 
   getCurrentBundleData() : any {
     return this.bundleData;
   }

   createBundle(data : any) {
     let url = this.kieSettings.fhirserverURL;
     let postData = data;
     const headers = { };
     return this.http.post(url, postData, { headers });
   } 

   updateKieSettings(kieSettings: KieSettings,bundleData : string) {
     this.kieSettings = kieSettings;
     this.bundleData = bundleData;
     this.cookieService.putObject("himms", this.kieSettings);
     localStorage.setItem("bundleData",this.bundleData);
  }

  getCurrentKieSettings(): KieSettings {
    return this.kieSettings;
  }

  submitClaim(claim : Claim,policy : Policy)
  {
    let postData = {
      tClaim : {
        "com.property_insurance.model.Claim" : claim
      },
      tPolicy : {
        "com.property_insurance.model.Policy" : policy
      }
    };

    postData.tClaim['com.property_insurance.model.Claim'].accidentDate = postData.tClaim['com.property_insurance.model.Claim'].accidentDate + "T15:49:05.630Z";
    postData.tClaim['com.property_insurance.model.Claim'].filingDate = postData.tClaim['com.property_insurance.model.Claim'].filingDate + "T15:49:05.630Z";

    let url = this.kieSettings.baseurl + "/rest/server/containers/"+this.kieSettings.picontainerAlias+"/processes/"+this.kieSettings.processId+"/instances";
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
    }
    return this.http.post(url,postData,{ headers });
  }

  getProcessInstances(type: string) {
    let status: number;
    if (type == "Active")
      status = 1;
    else
      status = 2;
    let url = this.kieSettings.baseurl + "/rest/server/containers/" + this.kieSettings.picontainerAlias + "/processes/instances?status=" + status + "&page=0&pageSize=100&sortOrder=true";
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("getProcessInstances() with status = "+type);
    return this.http.get(url, { headers });

  }

  getProcessInstanceVariables(processInstanceId: number) {
    let url = this.kieSettings.baseurl + "/fhir/processes/instance/" + processInstanceId + "/variables";
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("getProcessInstanceVariables() for pId = "+processInstanceId);
    return this.http.get(url, { headers });

  }

  getSVGImage(processInstanceId: number) {
    let url = this.kieSettings.baseurl + "/rest/server/containers/" + this.kieSettings.picontainerAlias + "/images/processes/instances/" + processInstanceId +
      "?svgCompletedColor=%23d8fdc1&svgCompletedBorderColor=%23030303&svgActiveBorderColor=%23FF0000";
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'accept': 'application/svg+xml',
      'content-type': 'application/svg+xml'
    };
    console.log("getSVGImage() for pId = "+processInstanceId);
    return this.http.get(url, { headers, responseType: 'text' });
  }


  getActiveTaskInstances(processInstanceId: number) {
    let url = this.kieSettings.baseurl + "/rest/server/queries/tasks/instances/process/" + processInstanceId;
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("getActiveTaskInstances() for pId = "+processInstanceId);
    return this.http.get(url, { headers });
  }

  getTaskVariables(taskInstanceId: number) {
    let url = this.kieSettings.baseurl + "/fhir/tasks/taskinstance/"+taskInstanceId+ "/variables";
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("getTaskVariables() for taskId = "+taskInstanceId);
    return this.http.get(url, { headers });
  }


  updateTaskStatus(taskInstanceId: number, taskStatus: string) {
    let url = this.kieSettings.baseurl + "/rest/server/containers/" + this.kieSettings.picontainerAlias + "/tasks/" + taskInstanceId + "/states/" + taskStatus + "?user=" + this.kieSettings.username;
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("updateTaskStatus() for taskId = "+taskInstanceId+" : taskStatus = "+taskStatus);
    return this.http.put(url, "", { headers });
  }

  updateVariables(taskInstanceId: number, data: any,cred : Credentials) {
    let url = this.kieSettings.baseurl + "/rest/server/containers/" + this.kieSettings.picontainerAlias + "/tasks/" + taskInstanceId + "/contents/output?user="+this.kieSettings.username;
    const headers = {
      'Authorization': 'Basic ' + btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };
    console.log("updateVariables() for taskId = "+taskInstanceId+" : data = "+data);
    return this.http.put(url, data, { headers });
  }

  signalEvent(processInstanceId : number,signalName : string,shipmentRequest : any)
  {
    let url = this.kieSettings.baseurl + "/rest/server/containers/" + this.kieSettings.picontainerAlias + "/processes/instances/"+processInstanceId + "/signal/" + signalName;
    let postData = shipmentRequest;
    const headers = {
      'Authorization': 'Basic ' +  btoa(this.kieSettings.username + ":" + this.kieSettings.password),
      'content-type': 'application/json',
      'X-KIE-ContentType': 'JSON',
      'accept': 'application/json'
    };

    console.log("signalEvent() for pInstanceId = "+processInstanceId+" : signalName = "+signalName+" : shipmentRequest = "+shipmentRequest);
    return this.http.post(url, postData, { headers });

  }


}
