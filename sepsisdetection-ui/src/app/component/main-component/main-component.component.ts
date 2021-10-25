import { Component, OnInit } from '@angular/core';
import { UserRole } from 'src/app/Models/UserRole';
import { faUser,faCog,faDatabase } from '@fortawesome/free-solid-svg-icons';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { BackendSettingsComponent } from 'src/app/component/Modals/BackendSettings/BackendSettings.component';
import { FhirSSEComponent } from 'src/app/component/FhirSSE/FhirSSE.component';
import { CompletedProcessInstanceComponent } from 'src/app/component/Modals/completed-process-instance/completed-process-instance.component';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';

 
@Component({
  selector: 'app-main-component',
  templateUrl: './main-component.component.html',
  styleUrls: ['./main-component.component.css']
})
export class MainComponent implements OnInit {


  user : UserRole;
  userList : UserRole[];
  selectedOption : string = "";
  userName : string = "";
  faUser = faUser;
  faCogs = faCog;
  faDatabase = faDatabase;

  public isLoggedIn = false;
  public isAdminUser = false;
  public userProfile: KeycloakProfile | null = null;

  constructor(private modalService: NgbModal, public readonly keycloak: KeycloakService) {
  }

 changeSelectedOption(option : UserRole)
  {
     if (option.userid == "logout" ) {
       this.logout();
     }
      
      this.user = option;
      this.selectedOption = option.name;
  }

  public async ngOnInit() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    this.isAdminUser = this.keycloak.isUserInRole("Administrators");
    console.log("ngOnInit() keycloak is logged in = "+this.isLoggedIn+" : isAdminUser = "+this.isAdminUser);

    if (this.isLoggedIn) {
      this.userProfile = await this.keycloak.loadUserProfile();
      console.log("NAME: "+(await this.keycloak.getUsername())+" "+(await this.keycloak.loadUserProfile()).lastName)

      if(!this.userProfile.firstName || !this.userProfile.lastName) {
        this.userName = this.userProfile.username;
      } else {
        this.userName = this.userProfile.firstName+" "+this.userProfile.lastName;
      }

      this.userList = [
        {
          id : 1,
          name : this.userName,
          role : this.keycloak.getUserRoles()[0],
          userid : this.keycloak.getUsername(),
          password : "Test123"
        },
        {
          id : 2,
          name : "Log Out",
          role : "",
          userid : "logout",
          password : ""
        },
      ];
   
      this.user = this.userList[0];
    } else {
      this.login();
    }
  }

  public login() {
    try {
      this.keycloak.login();
    }catch(error) {
      console.error("login() error = "+error);
      error.stack;
    }
  }

  public logout() {
    console.log("keycloak.logout()");
    this.keycloak.logout();
  }

  openSettings()
  {
    const modalRef = this.modalService.open(BackendSettingsComponent, { ariaLabelledBy: 'modal-basic-title', size: 'xl', backdrop: 'static' });
    
    modalRef.result.then((result) => {
     
      
    }, (reason) => {
      
    });
  }

  openClosedCases()
  {
    const modalRef = this.modalService.open(CompletedProcessInstanceComponent, { ariaLabelledBy: 'modal-basic-title', size: 'xl', backdrop: 'static' });
    
    modalRef.result.then((result) => {
     
      
    }, (reason) => {
      
    });
  }

}
