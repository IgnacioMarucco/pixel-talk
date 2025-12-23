import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';
import { TabsSharedComponent, TabsSharedItem } from '../../shared/tabs-shared/tabs-shared.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, ButtonSharedComponent, TabsSharedComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  readonly authService = inject(AuthService);
  readonly themeService = inject(ThemeService);
  readonly displayName = computed(() => {
    const name = this.authService.auth()?.username?.trim();
    return name || 'Profile';
  });
  readonly profileLink = computed(() => {
    const username = this.authService.auth()?.username?.trim();
    return username ? ['/u', username] : ['/feed'];
  });
  readonly navItems: TabsSharedItem[] = [
    { label: 'Feed', link: '/feed' },
    { label: 'Global', link: '/global' },
    { label: 'Trending', link: '/trending' }
  ];
  readonly themeLabel = computed(() => {
    const mode = this.themeService.mode();
    const theme = this.themeService.theme();
    if (mode === 'auto') {
      return 'Theme: Auto';
    }
    return `Theme: ${theme === 'dark' ? 'Dark' : 'Light'}`;
  });

  logout() {
    this.authService.logout();
  }

  toggleTheme() {
    this.themeService.toggleMode();
  }
}
