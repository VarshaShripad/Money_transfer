import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth-service';
import { AccountService, AccountResponse } from '../../services/account-service';
import { RewardService } from '../../services/reward-service';
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
  totalPoints?: number;
  targetAccountId?: number;
  isAdmin = false;
  loading = true;
  error = '';

  constructor(
    private auth: AuthService,
    private accountApi: AccountService,
    private rewardApi: RewardService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const id = this.auth.accountId;
    this.isAdmin = this.auth.isAdmin;

    if (!id) {
      this.error = 'No account found. Please login again.';
      this.loading = false;
      return;
    }

    this.loadAccountData(id);
  }

  loadAccountData(accountId?: number) {
    let resolvedId = Number(accountId);
    if (!resolvedId || resolvedId <= 0) {
      if (this.isAdmin) {
        resolvedId = this.auth.accountId ?? 0;
      }
    }

    if (!resolvedId || resolvedId <= 0) {
      this.error = 'Enter a valid account ID.';
      return;
    }

    this.error = '';
    this.loading = true;

    this.accountApi.getAccount(resolvedId).subscribe({
      next: (acc) => {
        this.account = acc;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load account info.';
        this.loading = false;
        this.cd.detectChanges();
      }
    });

    this.accountApi.getBalance(resolvedId).subscribe({
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

    if (this.isAdmin) {
      this.rewardApi.getRewardSummaryByUser(resolvedId).subscribe({
        next: (summary) => {
          this.totalPoints = summary.totalRewardPoints;
          this.cd.detectChanges();
        },
        error: () => {
          this.totalPoints = undefined;
          console.warn('Failed to load reward points for account', resolvedId);
        }
      });
    } else {
      this.rewardApi.getTotalPoints().subscribe({
        next: (pts) => {
          this.totalPoints = pts as number;
          this.cd.detectChanges();
        },
        error: () => {
          console.warn('Failed to load reward points');
        }
      });
    }
  }
}