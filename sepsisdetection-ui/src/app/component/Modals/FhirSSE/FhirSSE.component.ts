import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';


@Component({
  selector: 'fhir-sse',
  templateUrl: './FhirSSE.component.html',
  styleUrls: ['./FhirSSE.component.css']
})
export class FhirSSEComponent implements OnInit, OnDestroy {
  title = 'client';
  rawFhirStreamSubject: Observable<string[]>;

  constructor( private modalService: NgbModal) {
  }
  
  ngOnInit(): void {
  }
  ngOnDestroy(): void {
  }

  modalClose() {
    this.modalService.dismissAll();
  }

}
