import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AccountService, AccountResponse } from '../../services/account-service';
import { AuthService } from '../../services/auth-service';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.scss'
})

export class DashboardComponent implements OnInit {

  accountId!: number;
  account?: AccountResponse;
  balance?: number;

  loading = true;
  error = '';

  constructor(
    private accountApi: AccountService,
    private auth: AuthService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const id = this.auth.accountId;

    if (!id) {
      this.error = 'No account found. Please login again.';
      this.loading = false;
      return;
    }

    this.accountId = id;

    // get holderName from backend
    this.accountApi.getAccount(id).subscribe({
      next: (acc) => {
        this.account = acc;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load account details';
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
        this.error = 'Failed to load balance';
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  goTransfer() {
    this.router.navigateByUrl('/transfer');
  }

  goHistory() {
    this.router.navigateByUrl('/history');
  }

  goRewards() {
    this.router.navigateByUrl('/rewards');
  }

  goProfile() {
    this.router.navigateByUrl('/profile');
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
