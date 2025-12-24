import { inject, Injectable, signal, computed, effect } from '@angular/core';
import { Router } from '@angular/router';
import { MonoTypeOperatorFunction, tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthResponse, LoginRequest, RefreshTokenRequest, RegisterRequest } from '../models/auth.model';
import { RedirectService } from './redirect.service';

interface AuthState {
  accessToken: string;
  refreshToken: string;
  userId: number;
  username: string;
  email: string;
  roles: string[];
}

const STORAGE_KEY = 'cp_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);
  private redirectService = inject(RedirectService);

  private state = signal<AuthState | null>(this.loadFromStorage());

  readonly auth = this.state.asReadonly();
  readonly isAuthenticated = computed(() => !!this.state()?.accessToken);

  constructor() {
    effect(() => {
      const current = this.state();
      if (current) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(current));
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    });
  }

  login(request: LoginRequest) {
    return this.api.post<AuthResponse>('/v1/auth/login', request).pipe(
      this.tapAuthUpdate()
    );
  }

  register(request: RegisterRequest) {
    return this.api.post<AuthResponse>('/v1/auth/register', request).pipe(
      this.tapAuthUpdate()
    );
  }

  refreshToken() {
    const current = this.state();
    if (!current?.refreshToken) {
      throw new Error('Missing refresh token');
    }
    const payload: RefreshTokenRequest = { refreshToken: current.refreshToken };
    return this.api.post<AuthResponse>('/v1/auth/refresh', payload).pipe(
      this.tapAuthUpdate()
    );
  }

  logout() {
    const current = this.state();
    if (current?.refreshToken) {
      const payload: RefreshTokenRequest = { refreshToken: current.refreshToken };
      this.api.post<void>('/v1/auth/logout', payload).subscribe({
        next: () => this.clearAuth(),
        error: () => this.clearAuth()
      });
    } else {
      this.clearAuth();
    }
  }

  clearAuth() {
    this.state.set(null);
    this.redirectService.clear();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  getAccessToken(): string | null {
    return this.state()?.accessToken ?? null;
  }

  private tapAuthUpdate(): MonoTypeOperatorFunction<AuthResponse> {
    return tap((response) => {
      const next: AuthState = {
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        userId: response.userId,
        username: response.username,
        email: response.email,
        roles: response.roles ?? []
      };
      this.state.set(next);
    });
  }

  private loadFromStorage(): AuthState | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as AuthState;
      return this.hydrateFromToken(parsed);
    } catch {
      return null;
    }
  }

  private hydrateFromToken(state: AuthState): AuthState {
    if (!state?.accessToken) {
      return state;
    }

    const payload = this.decodeJwtPayload(state.accessToken);
    
    // Always update username from JWT if it exists in the token and not in state
    if (payload && typeof payload['username'] === 'string') {
      const currentUsername = state.username?.trim();
      if (!currentUsername) {
        state.username = payload['username'] as string;
      }
    }
    
    if (payload && !state.userId && typeof payload['sub'] === 'string') {
      const parsedId = Number(payload['sub']);
      if (!Number.isNaN(parsedId)) {
        state.userId = parsedId;
      }
    }
    return state;
  }

  private decodeJwtPayload(token: string): Record<string, unknown> | null {
    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const decoded = atob(padded);
      return JSON.parse(decoded) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
