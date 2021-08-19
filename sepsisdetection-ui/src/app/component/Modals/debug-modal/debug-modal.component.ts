import { Component, Input, OnInit } from '@angular/core';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JsonPipe } from '@angular/common';

@Component({
  selector: 'app-debug-modal',
  templateUrl: './debug-modal.component.html',
  styleUrls: ['./debug-modal.component.css']
})
export class DebugModalComponent implements OnInit {

  faTimesCircle = faTimesCircle;
  @Input() debug;
  jsonPipe : JsonPipe;

  constructor(public activeModal: NgbActiveModal,jsonPipe : JsonPipe) {
    this.jsonPipe =jsonPipe;

  }

  ngOnInit(): void {
    console.log(this.debug);
  }
  
  dismiss() {
    this.activeModal.dismiss();
  }

}
