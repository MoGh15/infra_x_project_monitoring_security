import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import {jwtInterceptor} from './core/auth/jwt.interceptor';
import {MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter} from '@angular/material/core';
import {DE_DATE_FORMATS} from './shared/validators/date-formats';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideRouter(routes),
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    { provide: MAT_DATE_FORMATS, useValue: DE_DATE_FORMATS },
    provideNativeDateAdapter(),
  ]
};
