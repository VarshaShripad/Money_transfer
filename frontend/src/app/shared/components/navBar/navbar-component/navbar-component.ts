import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-component.html',
  styleUrl: './navbar-component.scss'
})
export class NavbarComponent {

  constructor(private router: Router) {}

  goHome() {
    this.router.navigateByUrl('/dashboard');
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

  isActive(path: string): boolean {
    return this.router.url === path || this.router.url.startsWith(path + '/');
  }
}