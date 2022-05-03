import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JsonPipe } from '@angular/common';
import { CookieModule } from 'ngx-cookie';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HttpClientModule } from '@angular/common/http';
import { HighlightModule, HIGHLIGHT_OPTIONS,HighlightOptions } from 'ngx-highlightjs';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';

import { AppComponent } from './app.component';
import { MainComponent } from './component/main-component/main-component.component';
import { BackendSettingsComponent } from './component/Modals/BackendSettings/BackendSettings.component';
import { CompletedProcessInstanceComponent } from './component/Modals/completed-process-instance/completed-process-instance.component';
import { AdminComponent } from './component/Admin/Admin.component';
import { RiskEvaluvationComponent } from './component/Modals/RiskEvaluvation/RiskEvaluvation.component';
import { RiskMitigationComponent } from './component/Modals/RiskMitigation/RiskMitigation.component';
import { FhirSSEComponent } from './component/Modals/FhirSSE/FhirSSE.component';

import { FlexLayoutModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

function initializeKeycloak(keycloak: KeycloakService) {
  
  return () =>
    keycloak.init({
      config: {
        url: window['_env'].KEYCLOAK_URL,
        realm: window['_env'].SSO_REALM,
        clientId: window['_env'].SSO_CLIENT
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      },
    });
}

@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    BackendSettingsComponent,
    CompletedProcessInstanceComponent,
    AdminComponent,
    RiskEvaluvationComponent,
    RiskMitigationComponent,
    FhirSSEComponent
  ],
  imports: [
    KeycloakAngularModule,
    BrowserModule,
    HighlightModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    NgbModule,
    CommonModule,
    FormsModule,
    HttpClientModule,
    CookieModule.forRoot(),
    FlexLayoutModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule
  ],
  providers: [JsonPipe,{
    provide: HIGHLIGHT_OPTIONS,
    useValue: {
      coreLibraryLoader: () => import('highlight.js/lib/core'),
      languages: {
        xml: () => import('highlight.js/lib/languages/xml'),
        typescript: () => import('highlight.js/lib/languages/typescript'),
        scss: () => import('highlight.js/lib/languages/scss'),
        json: () => import('highlight.js/lib/languages/json'),
        css: () => import('highlight.js/lib/languages/css')
      }
    }
  },{
    provide: APP_INITIALIZER,
    useFactory: initializeKeycloak,
    multi: true,
    deps: [KeycloakService],
  }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
