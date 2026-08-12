import { Injectable, NgZone } from '@angular/core';
import { environment } from '../../../environments/environment';

import { Observable } from 'rxjs';

import {TokenStorageService} from '../../core/auth/token-storage.service';
import {SubmissionListItem} from '../../core/api/submission.model';

type SseEvent =
  | { type: 'created'; payload: SubmissionListItem }
  | { type: 'updated'; payload: SubmissionListItem };

@Injectable({ providedIn: 'root' })
export class AdminSubmissionsSseService {
  constructor(
    private tokenStorage: TokenStorageService,
    private zone: NgZone
  ) {}

  connect(): Observable<SseEvent> {
    return new Observable<SseEvent>((subscriber) => {
      const token = this.tokenStorage.token;
      if (!token) {
        subscriber.complete();
        return;
      }

      const url = `${environment.apiUrl}/api/admin/submissions/stream?access_token=${encodeURIComponent(token)}`;
      const es = new EventSource(url);

      const onCreated = (ev: MessageEvent) => {
        this.zone.run(() => {
          try {
            subscriber.next({ type: 'created', payload: JSON.parse(ev.data) });
          } catch {
            // fallback: ignorieren
          }
        });
      };

      const onUpdated = (ev: MessageEvent) => {
        this.zone.run(() => {
          try {
            subscriber.next({ type: 'updated', payload: JSON.parse(ev.data) });
          } catch {}
        });
      };

      es.addEventListener('created', onCreated as any);
      es.addEventListener('updated', onUpdated as any);

      es.onerror = () => {
        // EventSource reconnectet automatisch. Wenn du willst: subscriber.error(...)
        // Wir lassen es bewusst laufen.
      };

      return () => {
        es.close();
      };
    });
  }
}
