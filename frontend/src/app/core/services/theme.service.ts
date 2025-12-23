import { DOCUMENT } from '@angular/common';
import { computed, effect, inject, Injectable, signal } from '@angular/core';

type ThemeMode = 'auto' | 'light' | 'dark';
type ThemeName = 'light' | 'dark';

const STORAGE_KEY = 'cp_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly modeSignal = signal<ThemeMode>(this.loadMode());
  private readonly systemPrefersDark = signal<boolean>(this.getSystemPrefersDark());

  readonly mode = this.modeSignal.asReadonly();
  readonly theme = computed<ThemeName>(() => {
    const mode = this.modeSignal();
    if (mode === 'auto') {
      return this.systemPrefersDark() ? 'dark' : 'light';
    }
    return mode;
  });

  constructor() {
    this.attachSystemListener();

    effect(() => {
      const mode = this.modeSignal();
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, mode);
      }
    });

    effect(() => {
      const isDark = this.theme() === 'dark';
      const body = this.document.body;
      if (!body) {
        return;
      }
      body.classList.toggle('theme-dark', isDark);
    });
  }

  toggleMode() {
    const mode = this.modeSignal();
    if (mode === 'auto') {
      const next = this.theme() === 'dark' ? 'light' : 'dark';
      this.modeSignal.set(next);
      return;
    }
    this.modeSignal.set('auto');
  }

  private attachSystemListener() {
    if (typeof window === 'undefined' || !window.matchMedia) {
      return;
    }
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    this.systemPrefersDark.set(mediaQuery.matches);
    mediaQuery.addEventListener('change', (event) => {
      this.systemPrefersDark.set(event.matches);
    });
  }

  private loadMode(): ThemeMode {
    if (typeof localStorage === 'undefined') {
      return 'auto';
    }
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'auto' || stored === 'light' || stored === 'dark') {
      return stored;
    }
    return 'auto';
  }

  private getSystemPrefersDark(): boolean {
    if (typeof window === 'undefined' || !window.matchMedia) {
      return false;
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
}
