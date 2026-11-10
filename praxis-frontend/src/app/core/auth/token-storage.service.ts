import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly KEY = 'access_token';

  get token(): string | null {
    return localStorage.getItem(this.KEY);
  }

  set token(value: string | null) {
    if (!value) localStorage.removeItem(this.KEY);
    else localStorage.setItem(this.KEY, value);
  }

  clear(): void {
    localStorage.removeItem(this.KEY);
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }
}
