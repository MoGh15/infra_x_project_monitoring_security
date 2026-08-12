import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { ConsentService } from '../../core/consent/consent.service';

@Component({
  selector: 'app-consent-overlay',
  imports: [CommonModule],
  templateUrl: './consent-overlay.component.html',
  styleUrl: './consent-overlay.component.scss'
})
export class ConsentOverlayComponent {
  protected readonly preferencesEnabled = signal(false);

  constructor(protected readonly consentService: ConsentService) {}

  acceptAll(): void {
    this.consentService.setAll();
  }

  acceptNecessary(): void {
    this.consentService.setNecessaryOnly();
  }

  saveSelection(): void {
    this.consentService.setPreferences(this.preferencesEnabled());
  }
}
