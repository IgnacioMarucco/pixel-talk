import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
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
  readonly displayName = computed(() => {
    const name = this.authService.auth()?.username?.trim();
    return name || 'Profile';
  });
  readonly navItems: TabsSharedItem[] = [
    { label: 'Feed', link: '/feed' },
    { label: 'Global', link: '/global' },
    { label: 'Trending', link: '/trending' }
  ];

  logout() {
    this.authService.logout();
  }
}
