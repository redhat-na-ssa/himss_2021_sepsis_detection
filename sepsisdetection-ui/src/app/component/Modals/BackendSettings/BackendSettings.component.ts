import { Component, OnInit } from '@angular/core';
import { BackendSettings } from 'src/app/Models/BackendSettings/BackendSettings';
import { BackendServices } from 'src/app/service/BackendServices';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-BackendSettings',
  templateUrl: './BackendSettings.component.html',
  styleUrls: ['./BackendSettings.component.css']
})
export class BackendSettingsComponent implements OnInit {

  backendService : BackendServices;
  backendSettings : BackendSettings;
  bundleData : string;
 

  constructor(backendService : BackendServices,public activeModal: NgbActiveModal) { 
    this.backendService = backendService;
    this.backendSettings = backendService.getCurrentBackendSettings();
    this.bundleData = backendService.getCurrentBundleData();
   
  }

  updateBackendSettings()
  {
      this.backendService.updateBackendSettings(this.backendSettings,this.bundleData);
      window.alert("Settings Saved Successfully");
  }

  ngOnInit() {
    this.backendSettings = this.backendService.getCurrentBackendSettings();
    console.log(this.backendSettings);
  }

  dismiss()
  {
    this.activeModal.dismiss();
  }

}
