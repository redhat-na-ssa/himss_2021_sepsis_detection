import { Component, OnInit } from '@angular/core';
import { UserRole } from 'src/app/Models/UserRole';
import { faUser,faCog,faDatabase, faSignInAlt, faSignOutAlt, faArrowCircleLeft } from '@fortawesome/free-solid-svg-icons';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { BackendSettingsComponent } from 'src/app/component/Modals/BackendSettings/BackendSettings.component';
import { CompletedProcessInstanceComponent } from 'src/app/component/Modals/completed-process-instance/completed-process-instance.component';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';
import { faAngular } from '@fortawesome/free-brands-svg-icons';

 
@Component({
  selector: 'app-main-component',
  templateUrl: './main-component.component.html',
  styleUrls: ['./main-component.component.css']
})
export class MainComponent implements OnInit {


  user : UserRole;
  userList : UserRole[];
  displayRoles : string[];
  userRoles : string[];
  selectedOption : string = "";
  userName : string = "";
  userRole : string = "";
  i : number = 0;
  faUser = faUser;
  faCogs = faCog;
  faDatabase = faDatabase;
  faSignIn = faSignInAlt;
  faSignOut = faSignOutAlt;
  faArrowCircleLeft = faArrowCircleLeft;

  public isLoggedIn = false;
  public isAdminUser = false;
  public userProfile: KeycloakProfile | null = null;

  constructor(private modalService: NgbModal, public readonly keycloak: KeycloakService) {
    this.displayRoles = [
      "admin",
      "doctor",
      "provider"
    ];
  }

 changeSelectedOption(option : UserRole)
  {
    if (option.userid == "logout" ) {
      this.logout();
    }
     
     this.user = option;
     this.selectedOption = option.name;    }

  public async ngOnInit() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    this.isAdminUser = this.keycloak.isUserInRole("Administrators");
    console.log("ngOnInit() keycloak is logged in = "+this.isLoggedIn+" : isAdminUser = "+this.isAdminUser);

    if (this.isLoggedIn) {
      this.userProfile = await this.keycloak.loadUserProfile();
      console.log("NAME: "+(await this.keycloak.getUsername())+" "+(await this.keycloak.loadUserProfile()).lastName)

      // Get display name for user
      if(!this.userProfile.firstName || !this.userProfile.lastName) {
        this.userName = this.userProfile.username;
      } else {
        this.userName = this.userProfile.firstName+" "+this.userProfile.lastName;
      }

      // Get display role for user (choose first assigned role that's also in display list)
      this.userRoles = this.keycloak.getUserRoles();
      this.userRole = this.userRoles[0];
      for(this.i=0;this.i<this.userRoles.length;this.i++) {
        if(this.displayRoles.indexOf(this.userRoles[this.i]) !== -1) {
          this.userRole = this.userRoles[this.i];
          break;
        }
      }

      this.userList = [
        {
          id : 1,
          name : this.userName,
          role : "( "+this.userRole+" )",
          userid : this.keycloak.getUsername(),
          password : "faUser"
        },
        {
          id : 2,
          name : "Log Out",
          role : "",
          userid : "logout",
          password : "faArrowCircleLeft"
        }
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
