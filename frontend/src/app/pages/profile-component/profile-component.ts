import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth-service';
import { AccountService, AccountResponse } from '../../services/account-service';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './profile-component.html',
  styleUrl: './profile-component.scss'
})
export class ProfileComponent implements OnInit {

  account?: AccountResponse;
  balance?: number;
  loading = true;
  error = '';

  constructor(
    private auth: AuthService,
    private accountApi: AccountService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const id = this.auth.accountId;

    if (!id) {
      this.error = 'No account found. Please login again.';
      this.loading = false;
      return;
    }

    this.accountApi.getAccount(id).subscribe({
      next: (acc) => {
        this.account = acc;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load account info.';
        this.cd.detectChanges();
      }
    });

    this.accountApi.getBalance(id).subscribe({
      next: (bal) => {
        this.balance = bal;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load balance.';
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }
}