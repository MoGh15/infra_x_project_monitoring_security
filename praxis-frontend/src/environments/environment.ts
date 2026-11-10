declare global {
  interface Window { __env?: { apiUrl?: string }; }
}

export const environment = {
  production: false,
  apiUrl: window.__env?.apiUrl ?? 'http://localhost:8080',
};
