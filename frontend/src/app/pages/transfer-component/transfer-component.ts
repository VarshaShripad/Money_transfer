import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { TransferService, TransferResponse } from '../../services/transfer-service';
import { uuidv4 } from '../../shared/pipes/utils/uuid';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';
import { AccountService, AccountResponse } from '../../services/account-service';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './transfer-component.html',
  styleUrl: './transfer-component.scss'
})
export class TransferComponent {
  fromAccountId!: number;
  account?: AccountResponse;
  holderName = '';

  loading = false;
  error = '';
  success?: TransferResponse;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    public auth: AuthService,
    private accountApi: AccountService,
    private transferApi: TransferService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {
    const id = this.auth.accountId;
    if (id) this.fromAccountId = id;

    this.form = this.fb.group({
      fromAccountId: [this.fromAccountId ?? null, [Validators.required, Validators.min(1)]],
      toAccountId: [null as number | null, [Validators.required, Validators.min(1)]],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]]
    });

    if (!this.auth.isAdmin) {
      this.form.get('fromAccountId')?.disable({ emitEvent: false });
     }// else {
    //   this.form.get('fromAccountId')?.enable({ emitEvent: false });
    // }
    if (id) {
      this.accountApi.getAccount(id).subscribe({
        next: (acc) => {
          this.account = acc;
          this.holderName = acc.holderName;   
          this.cd.detectChanges();
        },
        error: () => {
          this.holderName = ''; 
          this.cd.detectChanges();
        }
      });
    }
  }

  get usernameInitial(): string {
    const base = this.holderName || this.auth.username || '';
    return base.charAt(0).toUpperCase();
  }

  submit() {
    this.error = '';
    this.success = undefined;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const raw = this.form.getRawValue(); // 

    const body = {
      fromAccountId: raw.fromAccountId!,
      toAccountId: raw.toAccountId!,
      amount: raw.amount!,
      idempotencyKey: uuidv4()
    };

    this.transferApi.transferSecure(body).subscribe({
      next: (res) => {
        this.loading = false;
        this.success = res;
        this.cd.detectChanges();
        this.form.reset({ fromAccountId: raw.fromAccountId, toAccountId: null, amount: null });
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Transfer failed.';
        this.cd.detectChanges();
      }
    });
  }

  cancel() {
    this.router.navigateByUrl('/dashboard');
  }
}
