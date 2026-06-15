import { Component,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

type Mode = 'LOGIN' | 'SIGNUP';

type LoginForm = FormGroup<{
  holderName: FormControl<string>;
  username: FormControl<string>;
  password: FormControl<string>;
}>;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.scss'
})
export class LoginComponent {
  mode: Mode = 'LOGIN';

  loading = false;
  error = '';
  success = '';
  submitted = false;

  form: LoginForm;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      holderName: this.fb.nonNullable.control('', []),
      username: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(30),
        Validators.pattern(/^[a-zA-Z0-9._-]+$/)
      ]),
      password: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50)
      ])
    });

    this.applyModeValidators();
  }

  get holderNameCtrl() { return this.form.controls.holderName; }
  get usernameCtrl() { return this.form.controls.username; }
  get passwordCtrl() { return this.form.controls.password; }

  showError(ctrl: FormControl<any>) {
    return (ctrl.touched || this.submitted) && ctrl.invalid;
  }

  // Reset everything when switching tabs
  toggleMode() {
    if (this.loading) return;

    this.mode = this.mode === 'LOGIN' ? 'SIGNUP' : 'LOGIN';

    // clear messages + submitted state
    this.error = '';
    this.success = '';
    this.submitted = false;

    // reset form values + touched state
    this.form.reset({
      holderName: '',
      username: '',
      password: ''
    });

    // re-apply validators for new mode
    this.applyModeValidators();

    this.cd.detectChanges();
  }

  private applyModeValidators() {
    if (this.mode === 'SIGNUP') {
      this.holderNameCtrl.setValidators([
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50)
      ]);
    } else {
      this.holderNameCtrl.clearValidators();
    }
    this.holderNameCtrl.updateValueAndValidity({ emitEvent: false });
  }

  private extractErrorMessage(err: any): string {
    // Spring error response: { errorCode, message }
    if (err?.error?.message && typeof err.error.message === 'string') return err.error.message;

    // Validation errors: { errors: [...] }
    if (Array.isArray(err?.error?.errors) && err.error.errors.length) return err.error.errors[0];

    // Plain text errors (responseType text)
    if (typeof err?.error === 'string' && err.error.trim()) return err.error;

    // HttpClient status text
    if (typeof err?.message === 'string' && err.message.trim()) return err.message;

    return `Login failed (status ${err?.status ?? 'unknown'}).`;
  }

  submit() {
    this.submitted = true;
    this.error = '';

    if (this.mode === 'LOGIN') this.success = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const username = this.usernameCtrl.value.trim();
    const password = this.passwordCtrl.value;

    if (this.mode === 'LOGIN') {
      this.auth.login({ username, password }).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigateByUrl('/dashboard');
          this.cd.detectChanges();
        },
        error: (err) => {
          this.loading = false;
          this.error = this.extractErrorMessage(err);
          this.cd.detectChanges();
        }
      });
      return;
    }

    // SIGNUP
    const holderName = this.holderNameCtrl.value.trim();

    this.auth.signup({ username, password, holderName }).subscribe({
      next: (resText: any) => {
        this.loading = false;

        this.success = (typeof resText === 'string' && resText.trim())
          ? resText
          : 'User registered successfully';

        this.mode = 'LOGIN';
        this.error = '';
        this.submitted = false;

        this.form.reset({
          holderName: '',
          username: '',
          password: ''
        });

        this.applyModeValidators();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.error = this.extractErrorMessage(err);
        this.cd.detectChanges();
      }
    });
  }
}
