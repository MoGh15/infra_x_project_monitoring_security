import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ConsentStatus = 'necessary' | 'all';

export interface ConsentState {
  status: ConsentStatus;
  preferences: boolean;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class ConsentService {
  private readonly STORAGE_KEY = 'consent_v1';
  private readonly stateSubject = new BehaviorSubject<ConsentState | null>(
    this.readFromStorage()
  );

  readonly state$ = this.stateSubject.asObservable();

  get state(): ConsentState | null {
    return this.stateSubject.value;
  }

  get hasDecision(): boolean {
    return !!this.stateSubject.value;
  }

  setNecessaryOnly(): void {
    this.save({ status: 'necessary', preferences: false });
  }

  setAll(): void {
    this.save({ status: 'all', preferences: true });
  }

  setPreferences(preferences: boolean): void {
    this.save({ status: preferences ? 'all' : 'necessary', preferences });
  }

  private save(next: Omit<ConsentState, 'timestamp'>): void {
    const payload: ConsentState = {
      ...next,
      timestamp: new Date().toISOString()
    };

    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(payload));
    this.stateSubject.next(payload);
  }

  private readFromStorage(): ConsentState | null {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    if (!raw) return null;

    try {
      const parsed = JSON.parse(raw) as ConsentState;
      if (parsed?.status && typeof parsed.preferences === 'boolean') {
        return parsed;
      }
    } catch {
      return null;
    }

    return null;
  }
}
