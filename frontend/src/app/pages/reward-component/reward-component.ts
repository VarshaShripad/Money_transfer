import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';
import { RewardService, RewardSummary, RewardEntry } from '../../services/reward-service';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './reward-component.html',
  styleUrl: './reward-component.scss'
})
export class RewardComponent implements OnInit {

  rewardSummary?: RewardSummary;
  loading = true;
  error = '';
  targetAccountId?: number;
  isAdmin = false;
  viewAccountId?: number;

  constructor(
    private rewardService: RewardService,
    private auth: AuthService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.isAdmin = this.auth.isAdmin;
    this.viewAccountId = this.auth.accountId ?? undefined;
    this.loadRewards(this.auth.accountId ?? undefined);
  }

  loadRewards(accountId?: number) {
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
    this.viewAccountId = resolvedId;

    const request = this.isAdmin
      ? this.rewardService.getRewardSummaryByUser(resolvedId)
      : this.rewardService.getRewardSummary();

    request.subscribe({
      next: (summary) => {
        this.rewardSummary = summary;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load rewards. Please try again later.';
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  trackByReward(index: number, reward: RewardEntry) {
    return reward.id;
  }

  goTransfer() {
    this.router.navigateByUrl('/transfer');
  }

  goHistory() {
    this.router.navigateByUrl('/history');
  }

  goDashboard() {
    this.router.navigateByUrl('/dashboard');
  }
}
