import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { KieSettings } from 'src/app/Models/KieSettings/KieSettings';
import { TaskInstance } from 'src/app/Models/Requests/Request';
import { UserRole } from 'src/app/Models/UserRole';
import { PAMServices } from 'src/app/service/PAMServices';


@Component({
  selector: 'app-RiskEvaluvation',
  templateUrl: './RiskEvaluvation.component.html',
  styleUrls: ['./RiskEvaluvation.component.css']
})
export class RiskEvaluvationComponent implements OnInit {

  baseurl : string = "";
  pamService : PAMServices;
  taskResponse : any;
  taskInstance : TaskInstance;
  patient : any;
  riskEvaluation : string = "";
  currentUser : UserRole;
  kieSettings : KieSettings;
  urlSafe: SafeResourceUrl;
  sanitizer: DomSanitizer;


  constructor(pamService : PAMServices,public activeModal: NgbActiveModal, sanitizer: DomSanitizer) { 
    this.pamService = pamService;
    this.kieSettings = pamService.getCurrentKieSettings();
    this.sanitizer = sanitizer;
    //console.log(this.taskInstance);
   // this.patient = JSON.parse(this.taskResponse.patient);
  
  }


  ngOnInit() {
    this.urlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(this.kieSettings.patientViewerURL);
    console.log(this.taskInstance);
    //this.patient = JSON.parse(this.taskResponse.patient);
    //this.patient.id = 12724066;
    // Sample URL : https://my.healthflow.io/patient-chart?patientId=12724066 
    
   // console.log(this.patient);
  }
  

  dismiss()
  {
    this.activeModal.dismiss();
  }

  onSubmit()
  {
    this.pamService.updateTaskStatus(this.taskInstance.taskId,"started").subscribe((data :any) => {
      this.pamService.updateVariables(this.taskInstance.taskId,{riskEvaluvationResult : this.riskEvaluation},null).subscribe((resp : any) => {
        this.pamService.updateTaskStatus(this.taskInstance.taskId,"completed").subscribe((data : any)=>{
          console.log("Updated : " + data);
          this.dismiss();
        });
      });
    });
      
  }

  onAbort()
  {
    this.pamService.signalEvent(this.taskInstance.processInstanceId,"Stop Process",{}).subscribe((res : any) => {
      console.log("Process Aborted : " + this.taskInstance.processInstanceId);
      this.dismiss();
    });
  }

}
