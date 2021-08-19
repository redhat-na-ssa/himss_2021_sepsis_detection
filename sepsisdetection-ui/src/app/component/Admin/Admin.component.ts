import { Component, OnInit, ElementRef, ViewChild ,Input} from '@angular/core';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { ProcessInstanceList,TaskInstanceList, TaskInstance } from 'src/app/Models/Requests/Request';
import { PAMServices } from 'src/app/service/PAMServices';
import { UserRole } from 'src/app/Models/UserRole';
import { RiskEvaluvationComponent } from '../Modals/RiskEvaluvation/RiskEvaluvation.component';
import { RiskMitigationComponent } from '../Modals/RiskMitigation/RiskMitigation.component';

@Component({
  selector: 'app-Admin',
  templateUrl: './Admin.component.html',
  styleUrls: ['./Admin.component.css']
})
export class AdminComponent implements OnInit {

  @ViewChild("svgContent") svgContentElement: ElementRef;
  @ViewChild("svgContentSubProcess") svgSubContentElement: ElementRef;
  @ViewChild("svgContentClosed") svgContentElementClosed: ElementRef;
  @Input() user : UserRole;
  closeResult: string = "";
  activeProcessInstances: ProcessInstanceList[] = new Array();
  closedProcessInstances: ProcessInstanceList[] = new Array();
  activeManagerTasks : TaskInstanceList = {
    instanceList : new Array()
  };
  svgContent : string = "";
  allowSvgContent : boolean = false;
  service: PAMServices;
 

   constructor(private modalService: NgbModal,service : PAMServices) {
      this.service = service;
   }

  ngOnInit(): void {
    this.getCaseList();
  }

  getCaseList()
  {
    this.activeProcessInstances = new Array();
    this.activeManagerTasks.instanceList = new Array();
    this.service.getProcessInstances("Active").subscribe((res: any) => {
      this.buildCaseList(res, this.activeProcessInstances, "Active");
    }, err => { console.log(err) });
  }

  private buildCaseList(response: any, caseList: ProcessInstanceList[], type: string) {
    let currentStatus = "Active";
    if (type != "Active")
      currentStatus = "Completed";

    if (response["process-instance"] && response["process-instance"] instanceof Array) {
      response["process-instance"].forEach((instance: any) => {
        let processInstance: ProcessInstanceList = {
          processInstanceId: instance["process-instance-id"],
          status: currentStatus,
          startedDate: instance["start-date"]["java.util.Date"],
          name: instance["process-instance-desc"]
        }
        if(instance["parent-instance-id"] == -1)
            caseList.push(processInstance);
        else
            {
              caseList.forEach((localInstance : ProcessInstanceList) =>
              {
                  if(localInstance.processInstanceId == instance["parent-instance-id"])
                  {
                      localInstance.subProcess = instance;
                  }
              });
            }    
      });
      caseList = caseList.sort((a: ProcessInstanceList, b: ProcessInstanceList) => {
        if (a.processInstanceId >= b.processInstanceId)
          return -1;
        else
          return 1;
      });

      if (type == "Active")
        this.buildVariablesList(caseList);
    }
  }

  private buildVariablesList(caseList: ProcessInstanceList[]) {
    caseList.forEach((currentInstance: ProcessInstanceList) => {
      this.service.getProcessInstanceVariables(currentInstance.processInstanceId).subscribe((res: any) => {
         this.mapVariableNameValue(res,currentInstance);
         this.onGetActiveTask(currentInstance.processInstanceId,this.user.userid,currentInstance);
      }, err => {

      });
    });

  }

  private mapVariableNameValue(res : any,caseInstance : ProcessInstanceList)
  {
    if(res.observation)
    {
        let observationObj = JSON.parse(res.observation)
        caseInstance.resourceType = observationObj.resourceType;
        caseInstance.id = observationObj.id;
        caseInstance.eventStatus = observationObj.status;
        if(observationObj.code && observationObj.code.coding && observationObj.code.coding instanceof Array)
        {
            caseInstance.code = observationObj.code.text;
            caseInstance.value = observationObj.valueQuantity.value;
            caseInstance.unit = observationObj.valueQuantity.code;
        } 
    }
  }

  onShowFlow(processInstanceId : number,type : string,processInstance : ProcessInstanceList)
  {
      if(this.allowSvgContent)
        {
          this.allowSvgContent = false;
          return;
        }
      this.service.getSVGImage(processInstanceId).subscribe((res : any) => { 
        this.svgContent = res;
        if(type == "Active")
            this.svgContentElement.nativeElement.innerHTML = this.svgContent;
        else
            this.svgContentElementClosed.nativeElement.innerHTML = this.svgContent;

      },err=>{ console.error(err);});
      if(processInstance.subProcess)
      {
        this.service.getSVGImage(processInstance.subProcess["process-instance-id"]).subscribe((res : any) => { 
          this.svgSubContentElement.nativeElement.innerHTML = res;
        });
            
      }

      this.allowSvgContent = true;
  }



  onGetActiveTask(processInstanceId : number,userid : string,instance: ProcessInstanceList)
  {
      this.service.getActiveTaskInstances(processInstanceId).subscribe((res:any)=>{
          if(res["task-summary"] && res["task-summary"] instanceof Array)
          {
            res["task-summary"].forEach((task : any)=> {
                let taskInstance : TaskInstance = {
                  processInstanceId : processInstanceId,
                  taskCreatedDate : task["task-created-on"]["java.util.Date"],
                  taskId : task["task-id"],
                  taskName : task["task-name"],
                  taskSubject : task["task-subject"],
                  taskDescription  : task["task-description"]
                }

                //if(taskInstance.taskName == "Primary Doctor Evaluates Risk" || taskInstance.taskName == "On Call Doctor Evaluates Risk") 
                    this.activeManagerTasks.instanceList.push(taskInstance);
            });
          }
      },err=>{});
      if(instance.subProcess)
      {
        this.service.getActiveTaskInstances(instance.subProcess["process-instance-id"]).subscribe((res:any)=>{
          if(res["task-summary"] && res["task-summary"] instanceof Array)
          {
            res["task-summary"].forEach((task : any)=> {
                let taskInstance : TaskInstance = {
                  processInstanceId : processInstanceId,
                  taskCreatedDate : task["task-created-on"]["java.util.Date"],
                  taskId : task["task-id"],
                  taskName : task["task-name"],
                  taskSubject : task["task-subject"],
                  taskDescription  : task["task-description"]
                }

                //if(taskInstance.taskName == "Primary Doctor Evaluates Risk" || taskInstance.taskName == "On Call Doctor Evaluates Risk") 
                    this.activeManagerTasks.instanceList.push(taskInstance);
            });
          }
        });
      }
  }

  getTaskVaribles(taskid : number,taskInstance: TaskInstance)
  {
      this.service.getTaskVariables(taskid).subscribe((res:any) => {
        console.log(res);
        if(taskInstance.taskName == "Primary Doctor Evaluates Risk" || taskInstance.taskName == "On Call Doctor Evaluates Risk") 
            this.openRiskEvaluvation(res,taskInstance);
        else if(taskInstance.taskName == "Licensed Provider Determines Patient Disposition" || taskInstance.taskName == "Escalation Licensed Provider Disposition") 
            this.openRiskMitigation(res,taskInstance);
      },err => {})
  }


   private openRiskEvaluvation(response : any,taskInstance: TaskInstance) {
    const modalRef = this.modalService.open(RiskEvaluvationComponent, { ariaLabelledBy: 'modal-basic-title', size: 'xl', backdrop: 'static' });

    modalRef.result.then((result) => {
      this.getCaseList();
    }, (reason) => {
      this.closeResult = "Dismissed";
      this.getCaseList();
    });

    modalRef.componentInstance.taskResponse = response;
    modalRef.componentInstance.currentUser = this.user;
    modalRef.componentInstance.taskInstance = taskInstance;
  } 

  private openRiskMitigation(response : any,taskInstance : TaskInstance) {
    const modalRef = this.modalService.open(RiskMitigationComponent, { ariaLabelledBy: 'modal-basic-title', size: 'xl', backdrop: 'static' });

    modalRef.result.then((result) => {
      this.getCaseList();
    }, (reason) => {
      this.closeResult = "Dismissed";
      this.getCaseList();
    });

    modalRef.componentInstance.taskResponse = response;
    modalRef.componentInstance.currentUser = this.user;
    modalRef.componentInstance.taskInstance = taskInstance;
  }

  onAbort(instance : ProcessInstanceList)
  {
    this.service.signalEvent(instance.processInstanceId,"Stop Process",{}).subscribe((res : any) => {
      console.log("Process Aborted : " + instance.processInstanceId);
    });
  }

}
