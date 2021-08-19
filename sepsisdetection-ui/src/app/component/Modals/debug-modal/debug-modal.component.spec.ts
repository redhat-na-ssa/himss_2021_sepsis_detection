import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DebugModalComponent } from './debug-modal.component';

describe('DebugModalComponent', () => {
  let component: DebugModalComponent;
  let fixture: ComponentFixture<DebugModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DebugModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DebugModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
