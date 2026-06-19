import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface RewardEntry {
  id: string;
  userId: number;
  transactionId: string;
  rewardPoints: number;
  earnedOn: string;
}

export interface RewardSummary {
  userId: number;
  totalRewardPoints: number;
  rewards: RewardEntry[];
}

@Injectable({ providedIn: 'root' })
export class RewardService {
  constructor(private http: HttpClient) {}

  getRewardSummary() {
    return this.http.get<RewardSummary>('/api/v1/rewards/summary');
  }

  getRewardSummaryByUser(userId: number) {
    return this.http.get<RewardSummary>(`/api/v1/rewards/user/${userId}`);
  }

  getRewards() {
    return this.http.get<RewardEntry[]>('/api/v1/rewards');
  }

  getTotalPoints() {
    return this.http.get<number>('/api/v1/rewards/total');
  }
}
