import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { SubmissionCreateRequest } from './submission-create.model';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PublicSubmissionsService {
  private baseUrl = environment.apiUrl; // z.B. http://localhost:8080

  constructor(private http: HttpClient) {}

  createSubmission(payload: SubmissionCreateRequest, files?: File[]): Observable<HttpEvent<void>> {
    if (files && files.length) {
      const formData = new FormData();
      formData.append('payload', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      files.forEach(file => formData.append('files', file, file.name));
      return this.http.post<void>(`${this.baseUrl}/api/submissions`, formData, {
        observe: 'events',
        reportProgress: true,
      });
    }
    return this.http.post<void>(`${this.baseUrl}/api/submissions`, payload, {
      observe: 'events',
      reportProgress: true,
    });
  }
}
