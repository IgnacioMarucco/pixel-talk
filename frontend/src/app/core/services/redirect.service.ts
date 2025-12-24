import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'cp_redirect_url';

@Injectable({ providedIn: 'root' })
export class RedirectService {
  private readonly urlSignal = signal<string | null>(this.readStored());

  set(url: string) {
    const trimmed = url?.trim();
    if (!trimmed || trimmed.startsWith('/login')) {
      return;
    }
    this.urlSignal.set(trimmed);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(STORAGE_KEY, trimmed);
    }
  }

  consume(fallback: string): string {
    const current = this.urlSignal() ?? this.readStored();
    this.clear();
    return current || fallback;
  }

  clear() {
    this.urlSignal.set(null);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem(STORAGE_KEY);
    }
  }

  private readStored(): string | null {
    if (typeof sessionStorage === 'undefined') {
      return null;
    }
    return sessionStorage.getItem(STORAGE_KEY);
  }
}
