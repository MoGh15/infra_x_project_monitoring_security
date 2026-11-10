import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import {NgIf} from '@angular/common';

type DialogMode = 'SAVE_ONLY' | 'MARK_DONE';

@Component({
  standalone: true,
  selector: 'app-confirm-done-dialog',
  imports: [MatDialogModule, MatButtonModule, NgIf],
  template: `
    <h2 mat-dialog-title>{{ vm.title }}</h2>

    <mat-dialog-content>
      <p>{{ vm.message }}</p>
      <p class="hint" *ngIf="vm.hint">{{ vm.hint }}</p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>{{ vm.cancelText }}</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="true">
        {{ vm.confirmText }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .hint {
      color: rgba(0,0,0,0.7);
      font-size: 13px;
      margin-top: 8px;
    }
  `]
})
export class ConfirmDoneDialogComponent {
  vm: { title: string; message: string; hint?: string; confirmText: string; cancelText: string };

  constructor(@Inject(MAT_DIALOG_DATA) public data: { mode: DialogMode }) {
    if (data?.mode === 'SAVE_ONLY') {
      this.vm = {
        title: 'Änderungen speichern?',
        message: 'Möchten Sie Ihre Änderungen wirklich speichern?',
        confirmText: 'Speichern',
        cancelText: 'Abbrechen',
      };
    } else {
      this.vm = {
        title: 'Wirklich abschließen?',
        message: 'Änderungen werden gespeichert und die Submission wird als DONE markiert.',
        hint: 'Diese Aktion kann später wieder bearbeitet werden, bleibt aber als DONE gekennzeichnet.',
        confirmText: 'Speichern & DONE',
        cancelText: 'Abbrechen',
      };
    }
  }
}
