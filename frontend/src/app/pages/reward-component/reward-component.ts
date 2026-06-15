import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';
import { RewardService, RewardSummary, RewardEntry } from '../../services/reward-service';

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

  constructor(
    private rewardService: RewardService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadRewards();
  }

  loadRewards() {
    this.rewardService.getRewardSummary().subscribe({
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
