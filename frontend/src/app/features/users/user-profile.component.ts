import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { FollowService } from '../../core/services/follow.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonSharedComponent],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private followService = inject(FollowService);
  private fb = inject(FormBuilder);

  readonly username = input.required<string>();
  readonly user = signal<User | null>(null);
  readonly following = signal(false);
  readonly followersCount = signal(0);
  readonly followingCount = signal(0);
  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group({
    email: [''],
    firstName: [''],
    lastName: [''],
    profilePictureUrl: [''],
    bio: ['']
  });
  readonly isOwnProfile = computed(() => {
    const auth = this.authService.auth();
    const user = this.user();
    return !!auth && !!user && (auth.userId === user.id || auth.username?.trim() === user.username);
  });

  constructor() {
    effect(() => {
      const raw = this.username();
      const username = raw?.trim();
      if (!username) {
        this.user.set(null);
        this.following.set(false);
        this.followersCount.set(0);
        this.followingCount.set(0);
        this.errorMessage.set('User not found');
        this.loading.set(false);
        return;
      }
      this.loadUser(username);
    });
  }

  loadUser(username: string) {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.userService.getUserByUsername(username).subscribe({
      next: (user) => {
        this.user.set(user);
        this.form.patchValue({
          email: user.email ?? '',
          firstName: user.firstName ?? '',
          lastName: user.lastName ?? '',
          profilePictureUrl: user.profilePictureUrl ?? '',
          bio: user.bio ?? ''
        });
        this.loading.set(false);
        this.loadFollowState(user.id);
      },
      error: () => {
        this.errorMessage.set('Failed to load user');
        this.loading.set(false);
      }
    });
  }

  loadFollowState(userId: number) {
    if (!this.isOwnProfile()) {
      this.followService.isFollowing(userId).subscribe({
        next: (res) => this.following.set(res.following),
        error: () => this.following.set(false)
      });
    } else {
      this.following.set(false);
    }

    this.followService.getFollowersCount(userId).subscribe({
      next: (res) => this.followersCount.set(res.count),
      error: () => this.followersCount.set(0)
    });

    this.followService.getFollowingCount(userId).subscribe({
      next: (res) => this.followingCount.set(res.count),
      error: () => this.followingCount.set(0)
    });
  }

  toggleFollow() {
    const user = this.user();
    if (!user || this.isOwnProfile()) {
      return;
    }

    const action$ = this.following()
      ? this.followService.unfollowUser(user.id)
      : this.followService.followUser(user.id);

    action$.subscribe({
      next: () => {
        const nextState = !this.following();
        this.following.set(nextState);
        this.loadFollowState(user.id);
      },
      error: () => this.errorMessage.set('Failed to update follow')
    });
  }

  save() {
    const user = this.user();
    if (!user || !this.isOwnProfile()) {
      return;
    }

    this.saving.set(true);
    this.userService.updateCurrentUser(this.form.getRawValue()).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.saving.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to update profile');
        this.saving.set(false);
      }
    });
  }
}
