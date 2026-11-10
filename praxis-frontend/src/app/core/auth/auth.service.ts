import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string; // an dein Backend anpassen (z.B. "token")
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {}

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/api/auth/login`, req).pipe(
      tap(res => this.tokenStorage.token = res.accessToken)
    );
  }

  logout(): void {
    this.tokenStorage.clear();
  }

  isLoggedIn(): boolean {
    return this.tokenStorage.isLoggedIn();
  }
}
