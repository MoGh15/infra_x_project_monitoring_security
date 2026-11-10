import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import {SubmissionDetails, SubmissionListItem, SubmissionStatus} from './submission.model';
import { environment } from '../../../environments/environment';

type ApiSubmission = any;

@Injectable({ providedIn: 'root' })
export class AdminSubmissionsService {
  private readonly baseUrl = environment.apiUrl+'/api/admin/submissions';

  constructor(private http: HttpClient) {}

  list(): Observable<SubmissionListItem[]> {
    return this.http.get<ApiSubmission[]>(this.baseUrl).pipe(
      map(items => items.map(this.normalize))
    );
  }

  getById(id: string): Observable<SubmissionDetails> {
    return this.http.get<ApiSubmission>(`${this.baseUrl}/${id}`).pipe(
      map(this.normalize)
    );
  }

  update(id: string, payload: Partial<SubmissionDetails>) {
    return this.http.patch<SubmissionDetails>(`${this.baseUrl}/${id}`, payload);
  }


  updateStatus(id: string, status: SubmissionStatus): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/status`, { status });
  }

  downloadAttachment(submissionId: string, attachmentId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${submissionId}/attachments/${attachmentId}`, {
      responseType: 'blob',
    });
  }

  // Backend liefert oft _id -> wir machen einheitlich id
  private normalize = (s: ApiSubmission) => {
    const id = s.id ?? s._id;
    return { ...s, id } as any;
  };

}
