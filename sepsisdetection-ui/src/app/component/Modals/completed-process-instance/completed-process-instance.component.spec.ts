import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CompletedProcessInstanceComponent } from './completed-process-instance.component';

describe('CompletedProcessInstanceComponent', () => {
  let component: CompletedProcessInstanceComponent;
  let fixture: ComponentFixture<CompletedProcessInstanceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CompletedProcessInstanceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CompletedProcessInstanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
