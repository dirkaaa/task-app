import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../service/user.service';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {
  signUpForm: FormGroup;
  errorMessage: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {
    this.signUpForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });
  }

  passwordsMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  async onSubmit() {
    this.errorMessage = null;
    if (this.signUpForm.invalid) {
      this.signUpForm.markAllAsTouched();
      if (this.signUpForm.hasError('passwordMismatch')) {
        this.errorMessage = 'Passwords do not match.';
      }
      return;
    }
    this.loading = true;
    try {
      await this.userService.createUser({
        username: this.signUpForm.value.username,
        password: this.signUpForm.value.password
      });
      this.router.navigate(['/login']);
    } catch (err: any) {
      this.errorMessage = err?.error || 'Sign up failed. Please try again.';
    } finally {
      this.loading = false;
    }
  }
}