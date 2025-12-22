import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';
import {
  EMAIL_INVALID_MESSAGE,
  EMAIL_MAX_LENGTH,
  EMAIL_REQUIRED_MESSAGE,
  EMAIL_SIZE_MESSAGE,
  FIRST_NAME_SIZE_MESSAGE,
  LAST_NAME_SIZE_MESSAGE,
  NAME_MAX_LENGTH,
  PASSWORD_REQUIRED_MESSAGE,
  PASSWORD_SIZE_MESSAGE,
  USERNAME_MAX_LENGTH,
  USERNAME_MIN_LENGTH,
  USERNAME_PATTERN,
  USERNAME_PATTERN_MESSAGE,
  USERNAME_REQUIRED_MESSAGE,
  USERNAME_SIZE_MESSAGE
} from '../../core/validators/validation.constants';
import { PASSWORD_RULES, PasswordRuleKey, passwordValidator } from '../../core/validators/password.validator';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonSharedComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: [
      '',
      [
        Validators.required,
        Validators.minLength(USERNAME_MIN_LENGTH),
        Validators.maxLength(USERNAME_MAX_LENGTH),
        Validators.pattern(USERNAME_PATTERN)
      ]
    ],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(EMAIL_MAX_LENGTH)]],
    password: ['', [Validators.required, passwordValidator()]],
    firstName: ['', [Validators.maxLength(NAME_MAX_LENGTH)]],
    lastName: ['', [Validators.maxLength(NAME_MAX_LENGTH)]]
  });

  readonly passwordRules = PASSWORD_RULES;
  readonly messages = {
    usernameRequired: USERNAME_REQUIRED_MESSAGE,
    usernameSize: USERNAME_SIZE_MESSAGE,
    usernamePattern: USERNAME_PATTERN_MESSAGE,
    emailRequired: EMAIL_REQUIRED_MESSAGE,
    emailInvalid: EMAIL_INVALID_MESSAGE,
    emailSize: EMAIL_SIZE_MESSAGE,
    passwordRequired: PASSWORD_REQUIRED_MESSAGE,
    passwordSize: PASSWORD_SIZE_MESSAGE,
    firstNameSize: FIRST_NAME_SIZE_MESSAGE,
    lastNameSize: LAST_NAME_SIZE_MESSAGE
  };

  submit() {
    this.clearServerErrors();
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/feed']);
      },
      error: (err) => {
        this.applyServerErrors(err);
        this.loading.set(false);
      }
    });
  }

  isPasswordRuleMet(key: PasswordRuleKey): boolean {
    const value = this.form.controls.password.value;
    if (!value) {
      return false;
    }
    const errors = this.form.controls.password.errors;
    return !errors || !errors[key];
  }

  private applyServerErrors(err: unknown) {
    const fieldErrors = (err as { error?: { errors?: Record<string, string> } })?.error?.errors;
    if (fieldErrors && typeof fieldErrors === 'object') {
      this.setServerError(this.form.controls.username, fieldErrors['username']);
      this.setServerError(this.form.controls.email, fieldErrors['email']);
      this.setServerError(this.form.controls.password, fieldErrors['password']);
      this.setServerError(this.form.controls.firstName, fieldErrors['firstName']);
      this.setServerError(this.form.controls.lastName, fieldErrors['lastName']);
      this.errorMessage.set(null);
      return;
    }

    const detail = (err as { error?: { detail?: string } })?.error?.detail ?? 'Register failed';
    this.errorMessage.set(detail);
  }

  private setServerError(control: AbstractControl, message?: string) {
    if (!message) {
      return;
    }
    const existing = control.errors ?? {};
    control.setErrors({ ...existing, server: message });
    control.markAsTouched();
  }

  private clearServerErrors() {
    Object.values(this.form.controls).forEach((control) => {
      const errors = control.errors;
      if (!errors || !errors['server']) {
        return;
      }
      const { server, ...rest } = errors;
      control.setErrors(Object.keys(rest).length ? rest : null);
    });
    this.errorMessage.set(null);
  }
}
