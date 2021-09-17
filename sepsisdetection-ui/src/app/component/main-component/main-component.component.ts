import { Component, OnInit } from '@angular/core';
import { UserRole } from 'src/app/Models/UserRole';
import { faUser,faCog,faDatabase } from '@fortawesome/free-solid-svg-icons';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { BackendSettingsComponent } from 'src/app/component/Modals/BackendSettings/BackendSettings.component';
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
  faUser = faUser;
  faCogs = faCog;
  faDatabase = faDatabase;

  public isLoggedIn = false;
  public isAdminUser = false;
  public userProfile: KeycloakProfile | null = null;

  constructor(private modalService: NgbModal, public readonly keycloak: KeycloakService) {
    
    this.userList = [
      {
        id : 1,
        name : "Andrew Smith",
        role : "Admin",
        userid : "AndrewSmith",
        password : "test123"
      },
      {
        id : 2,
        name : "John Stark",
        role : "Doctor",
        userid : "JohnStark",
        password : "test123"
      },
      /* {
        id : 3,
        name : "Stacie Dorsey",
        role : "Approver",
        userid : "StacieDorsey",
        password : "test123"
      }  */
    ];

    this.user = this.userList[0];
  }

 changeSelectedOption(option : UserRole)
  {
      this.user = option;
      this.selectedOption = option.name;
  }

  public async ngOnInit() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    this.isAdminUser = this.keycloak.isUserInRole("Administrators");
    console.log("ngOnInit() keycloak is logged in = "+this.isLoggedIn+" : isAdminUser = "+this.isAdminUser);

    if (this.isLoggedIn) {
      this.userProfile = await this.keycloak.loadUserProfile();
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
