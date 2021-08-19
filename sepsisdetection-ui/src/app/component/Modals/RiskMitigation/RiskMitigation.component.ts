import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { KieSettings } from 'src/app/Models/KieSettings/KieSettings';
import { TaskInstance } from 'src/app/Models/Requests/Request';
import { UserRole } from 'src/app/Models/UserRole';
import { PAMServices } from 'src/app/service/PAMServices';

@Component({
  selector: 'app-RiskMitigation',
  templateUrl: './RiskMitigation.component.html',
  styleUrls: ['./RiskMitigation.component.css']
})
export class RiskMitigationComponent implements OnInit {

  baseurl : string = "";
  pamService : PAMServices;
  taskResponse : any;
  taskInstance : TaskInstance;
  patient : any;
  riskMitigationResp : string = "";
  currentUser : UserRole;
  kieSettings : KieSettings;
  urlSafe: SafeResourceUrl;

  constructor(pamService : PAMServices,public activeModal: NgbActiveModal,public sanitizer: DomSanitizer) { 
    this.pamService = pamService;
    this.kieSettings = pamService.getCurrentKieSettings();
    this.urlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(this.kieSettings.patientViewerURL);
  }


  ngOnInit() {
    console.log(this.taskInstance);
    this.patient = JSON.parse(this.taskResponse.patient);
   // console.log(this.patient);
  }
  

  dismiss()
  {
    this.activeModal.dismiss();
  }

  onSubmit()
  {
    this.pamService.updateTaskStatus(this.taskInstance.taskId,"started").subscribe((data :any) => {
      this.pamService.updateVariables(this.taskInstance.taskId,{dispositionResult : this.riskMitigationResp},null).subscribe((resp : any) => {
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
