import { Component, OnInit,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AccountService, TransactionResponse } from '../../services/account-service';
import { AuthService } from '../../services/auth-service';
import { NavbarComponent } from '../../shared/components/navBar/navbar-component/navbar-component';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './history-component.html',
  styleUrl: './history-component.scss'
})
export class HistoryComponent implements OnInit {

  txs: TransactionResponse[] = [];
  loading = true;
  error = '';
  accountId!: number;

  filter: 'ALL' | 'SENT' | 'RECEIVED' = 'ALL';

  constructor(
    private accountApi: AccountService,
    private auth: AuthService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const id = this.auth.accountId;

    if (!id) {
      this.error = 'No accountId found. Login again.';
      this.loading = false;
      return;
    }

    this.accountId = id;

    this.accountApi.getTransactions(id).subscribe({
      next: (res) => {
        this.txs = (res ?? []).sort((a, b) => {
          return new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime();
        });

        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load transactions.';
        this.loading = false;
        this.cd.detectChanges();
      }
    });

  }

  setFilter(value: 'ALL' | 'SENT' | 'RECEIVED') {
    this.filter = value;
  }

  // Determine sent/received dynamically
  isSent(tx: TransactionResponse) {
    return tx.fromAccountId === this.accountId;
  }

  isReceived(tx: TransactionResponse) {
    return tx.toAccountId === this.accountId;
  }

  get filteredTxs(): TransactionResponse[] {
    if (this.filter === 'ALL') return this.txs;

    if (this.filter === 'SENT') {
      return this.txs.filter(tx => this.isSent(tx));
    }

    return this.txs.filter(tx => this.isReceived(tx));
  }

  amountClass(tx: TransactionResponse) {
    return this.isSent(tx) ? 'amount debit' : 'amount credit';
  }

  badgeClass(status: string) {
    return status === 'SUCCESS' ? 'badge success' : 'badge failed';
  }
}