import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { BackendSettings } from 'src/app/Models/BackendSettings/BackendSettings';
import { TaskInstance } from 'src/app/Models/Requests/Request';
import { UserRole } from 'src/app/Models/UserRole';
import { BackendServices } from 'src/app/service/BackendServices';


@Component({
  selector: 'app-RiskEvaluvation',
  templateUrl: './RiskEvaluvation.component.html',
  styleUrls: ['./RiskEvaluvation.component.css']
})
export class RiskEvaluvationComponent implements OnInit {

  kieserverUrl : string = "";
  backendEndServices : BackendServices;
  taskResponse : any;
  taskInstance : TaskInstance;
  patient : any;
  riskEvaluation : string = "";
  currentUser : UserRole;
  backendSettings : BackendSettings;
  urlSafe: SafeResourceUrl;
  sanitizer: DomSanitizer;


  constructor(backendEndServices : BackendServices,public activeModal: NgbActiveModal, sanitizer: DomSanitizer) { 
    this.backendEndServices = backendEndServices;
    this.backendSettings = backendEndServices.getCurrentBackendSettings();
    this.sanitizer = sanitizer;
    //console.log(this.taskInstance);
   // this.patient = JSON.parse(this.taskResponse.patient);
  
  }


  ngOnInit() {
    this.urlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(this.backendSettings.patientViewerURL);
    console.log(this.taskInstance);
    //this.patient = JSON.parse(this.taskResponse.patient);
    //this.patient.id = 610f11c05b041e00082c54c2;
    // Sample URL : https://my.healthflow.io/patient-chart?patientId=610f11c05b041e00082c54c2 
    
   // console.log(this.patient);
  }
  

  dismiss()
  {
    this.activeModal.dismiss();
  }

  onSubmit()
  {
    this.backendEndServices.updateTaskStatus(this.taskInstance.taskId,"started").subscribe((data :any) => {
      this.backendEndServices.updateVariables(this.taskInstance.taskId,{riskEvaluvationResult : this.riskEvaluation},null).subscribe((resp : any) => {
        this.backendEndServices.updateTaskStatus(this.taskInstance.taskId,"completed").subscribe((data : any)=>{
          console.log("Updated : " + data);
          this.dismiss();
        });
      });
    });
      
  }

  onAbort()
  {
    this.backendEndServices.signalEvent(this.taskInstance.processInstanceId,"Stop Process",{}).subscribe((res : any) => {
      console.log("Process Aborted : " + this.taskInstance.processInstanceId);
      this.dismiss();
    });
  }

}
