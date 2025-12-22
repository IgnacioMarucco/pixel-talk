import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'feed',
        pathMatch: 'full'
      },
      {
        path: 'feed',
        loadComponent: () => import('./features/feed/feed.component').then(m => m.FeedComponent)
      },
      {
        path: 'global',
        loadComponent: () => import('./features/feed/feed.component').then(m => m.FeedComponent),
        data: { mode: 'global' }
      },
      {
        path: 'trending',
        loadComponent: () => import('./features/feed/feed.component').then(m => m.FeedComponent),
        data: { mode: 'trending' }
      },
      {
        path: 'posts/:id',
        loadComponent: () => import('./features/posts/post-detail.component').then(m => m.PostDetailComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
      },
      {
        path: 'users/:id',
        loadComponent: () => import('./features/users/user-profile.component').then(m => m.UserProfileComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'feed'
  }
];
