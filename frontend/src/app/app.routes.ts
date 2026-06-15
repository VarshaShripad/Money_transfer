import { Routes } from '@angular/router';
import { authGuard } from './core/auth-guard';

import { LoginComponent } from './pages/login-component/login-component';
import { DashboardComponent } from './pages/dashboard-component/dashboard-component';
import { TransferComponent } from './pages/transfer-component/transfer-component';
import { HistoryComponent } from './pages/history-component/history-component';
import { RewardComponent } from './pages/reward-component/reward-component';
import { ProfileComponent } from './pages/profile-component/profile-component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  { path: 'login', component: LoginComponent },
  { path: 'dashboard', canActivate: [authGuard], component: DashboardComponent },
  { path: 'transfer', canActivate: [authGuard], component: TransferComponent },
  { path: 'history', canActivate: [authGuard], component: HistoryComponent },
  { path: 'rewards', canActivate: [authGuard], component: RewardComponent },
  { path: 'profile', canActivate: [authGuard], component: ProfileComponent },
  { path: '**', redirectTo: 'login' }
];
