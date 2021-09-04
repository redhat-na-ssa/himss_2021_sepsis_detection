import { Component, OnInit,ElementRef, ViewChild  } from '@angular/core';
import { BackendServices } from 'src/app/service/BackendServices';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ProcessInstanceList } from 'src/app/Models/Requests/Request';

@Component({
  selector: 'app-completed-process-instance',
  templateUrl: './completed-process-instance.component.html',
  styleUrls: ['./completed-process-instance.component.css']
})
export class CompletedProcessInstanceComponent implements OnInit {

  @ViewChild("svgContent") svgContentElement: ElementRef;
  @ViewChild("svgContentSubProcess") svgSubContentElement: ElementRef;
  svgContent : string = "";
  allowSvgContent : boolean = false;

  service : BackendServices;
  closedProcessInstances: ProcessInstanceList[] = new Array();

  constructor(backendEndServices : BackendServices,public activeModal: NgbActiveModal) { 
    this.service = backendEndServices;
   
  }

  ngOnInit(): void {
    this.service.getProcessInstances("Completed").subscribe((res: any) => {
      this.buildCaseList(res, this.closedProcessInstances, "Completed");
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
          startedDate: instance["start-date"]["java.util.Date"]
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
            this.svgContentElement.nativeElement.innerHTML = this.svgContent;
      },err=>{ console.error(err);});
      this.allowSvgContent = true;
      if(processInstance.subProcess)
      {
        this.service.getSVGImage(processInstance.subProcess["process-instance-id"]).subscribe((res : any) => { 
          this.svgSubContentElement.nativeElement.innerHTML = res;
        });
            
      }
  }

  dismiss()
  {
    this.activeModal.dismiss();
  }

}
