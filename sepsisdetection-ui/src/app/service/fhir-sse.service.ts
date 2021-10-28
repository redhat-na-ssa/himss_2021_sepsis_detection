import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FhirSseService implements OnDestroy {

  rawFhirMessages: string[] = [];
  rawFhirStreamObservable!: Observable<string[]>;
  rawFhirStreamingUrl = window['_env'].FHIR_SSE_STREAMING_URL+"/sse/event/fhir/raw";
  rawFhirEventSource = null;
  rawFhirReconnectFrequencySeconds = 1;

  constructor(private zone: NgZone) { }

  ngOnDestroy(): void {
    
  }

   // https://stackoverflow.com/questions/36209784/variable-inside-settimeout-says-it-is-undefined-but-when-outside-it-is-defined
   rFwaitFunc = function() { return this.reconnectFrequencySeconds * 1000 };
   rFtryToSetupFunc = () => {
       this.rFsseConnect();
       this.rawFhirReconnectFrequencySeconds *= 2;
       if (this.rawFhirReconnectFrequencySeconds >= 64) {
           this.rawFhirReconnectFrequencySeconds = 64;
       }
   };

   rFreconnectFunc = () => {
     setTimeout(this.rFtryToSetupFunc, this.rFwaitFunc());
   }
 
   public rFsseConnect = () => {
     console.log("rFsseConnect() about to register for SSE at: "+this.rawFhirStreamingUrl);
 
       this.rawFhirEventSource = new EventSource(this.rawFhirStreamingUrl);
       this.rawFhirStreamObservable = new Observable<string[]>((observer) => {
         this.rawFhirEventSource.onopen = event => {
           this.zone.run(() => {
             this.rawFhirReconnectFrequencySeconds = 1;
           })
         };
         
         this.rawFhirEventSource.onmessage = event => {
           this.zone.run(() => {
             this.rawFhirMessages.push(event.data);
             observer.next(this.rawFhirMessages);
             //console.log("event = "+event.data);
           })
         };
         
         this.rawFhirEventSource.onerror = event => {
           this.zone.run(() => {
             console.log("sseConnect() ... will close and attempt re-connect to : "+this.rawFhirStreamingUrl);
             this.rawFhirEventSource.close();
             this.rFreconnectFunc();
           })
         };
 
       });
       
   }

   public flushFsseMessages = () => {
    this.rawFhirMessages = [];
   }

   public getRawFhirStreamObservable = () : Observable<string[]> => {
     return this.rawFhirStreamObservable;
   }
   
}
