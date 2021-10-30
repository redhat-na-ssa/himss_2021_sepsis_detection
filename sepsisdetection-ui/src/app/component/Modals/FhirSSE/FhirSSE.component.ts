import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';


@Component({
  selector: 'fhir-sse',
  templateUrl: './FhirSSE.component.html',
  styleUrls: ['./FhirSSE.component.css']
})
export class FhirSSEComponent implements OnInit, OnDestroy {
  title = 'client';
  rawFhirStreamSubject: Subject<string[]>;

  @ViewChild('fxLayout') fxLayout;

  constructor( private modalService: NgbModal) {
  }
  
  ngOnInit(): void {
    this.rawFhirStreamSubject.subscribe({
      next: (v) => {

        // Automatically scroll to bottom of modal when new event is streamed
        //console.log("FhirSSECompoent:  captured event; div scrollHeight = "+this.fxLayout.nativeElement.scrollHeight);
        this.fxLayout.nativeElement.scrollTop = this.fxLayout.nativeElement.scrollHeight;
      }
    });
  }
  ngOnDestroy(): void {
  }

  modalClose() {
    this.modalService.dismissAll();
  }

}
