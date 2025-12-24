import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';
import { RedirectService } from '../../core/services/redirect.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonSharedComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private redirectService = inject(RedirectService);

  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  readonly form = this.fb.nonNullable.group({
    usernameOrEmail: ['', Validators.required],
    password: ['', Validators.required]
  });

  submit() {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          if (!this.authService.isAuthenticated()) {
            this.errorMessage.set('Login failed. Invalid auth response.');
            return;
          }
          const returnUrl = this.redirectService.consume('/feed');
          this.router.navigateByUrl(returnUrl).catch(() => {
            this.errorMessage.set('Login succeeded but navigation failed.');
          });
        },
        error: (err) => {
          const detail = err?.error?.detail ?? 'Login failed';
          this.errorMessage.set(detail);
        }
      });
  }
}
