import { ObserversModule } from '@angular/cdk/observers';
import { HttpClient } from '@angular/common/http';
import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subscription } from 'rxjs';


@Component({
  selector: 'fhir-sse',
  templateUrl: './FhirSSE.component.html',
  styleUrls: ['./FhirSSE.component.css']
})
export class FhirSSEComponent implements OnInit, OnDestroy {
  title = 'client';
  message = '';
  messages: any[];
  rawFhirStreamingUrl = window['_env'].FHIR_SSE_STREAMING_URL+"/sse/event/fhir/raw";
  eventSource = null;
  reconnectFrequencySeconds = 1;

  constructor(private zone: NgZone, private http: HttpClient) {
  }

  waitFunc = function() { return this.reconnectFrequencySeconds * 1000 };
  tryToSetupFunc = () => {
      this.sseConnect();
      this.reconnectFrequencySeconds *= 2;
      if (this.reconnectFrequencySeconds >= 64) {
          this.reconnectFrequencySeconds = 64;
      }
  };
  reconnectFunc = () => {
    setTimeout(this.tryToSetupFunc, this.waitFunc());
  }

  sseConnect() {
    console.log("sseConnect() about to register for SSE at: "+this.rawFhirStreamingUrl);
    this.eventSource = new EventSource(this.rawFhirStreamingUrl);
    this.eventSource.onopen = event => {
      this.zone.run(() => {
        this.reconnectFrequencySeconds = 1;
      })
    };
    this.eventSource.onmessage = event => {
      this.zone.run(() => {
        this.addMessage(event.data);
      })
    };
    
    this.eventSource.onerror = event => {
      this.zone.run(() => {
        console.log("sseConnect() ... will close and attempt re-connect to : "+this.rawFhirStreamingUrl);
        this.eventSource.close();
        this.reconnectFunc();
      })
    };
  }

  ngOnInit(): void {
    this.messages = [];
    this.sseConnect();
  }

  addMessage(msg: any) {
    this.messages = [...this.messages, msg];
  }

  ngOnDestroy(): void {
  }

}
