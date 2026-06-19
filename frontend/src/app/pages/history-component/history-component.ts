import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
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
  viewAccountId!: number;
  isAdmin = false;
  accountNames = new Map<number, string>();

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
    this.viewAccountId = id;
    this.isAdmin = this.auth.isAdmin;

    this.loadTransactions(id);
  }

  loadTransactions(accountId: number) {
    const targetId = Number(accountId);
    const resolvedId = this.isAdmin && (!targetId || targetId <= 0)
      ? this.auth.accountId
      : targetId;

    if (!resolvedId || resolvedId <= 0) {
      this.error = 'Enter a valid account ID.';
      return;
    }

    this.error = '';
    this.loading = true;
    this.viewAccountId = resolvedId;

    this.accountApi.getTransactions(resolvedId).pipe(
      switchMap((txs) => {
        this.txs = (txs ?? []).sort((a, b) => new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime());

        const counterpartIds = Array.from(new Set(
          this.txs
            .flatMap(tx => [tx.fromAccountId, tx.toAccountId])
            .filter(accountId => accountId !== this.viewAccountId)
        ));

        if (!counterpartIds.length) {
          return of([]);
        }

        return forkJoin(
          counterpartIds.map((counterpartId) =>
            this.accountApi.getAccount(counterpartId).pipe(
              map((acc) => ({ id: counterpartId, name: acc.holderName })),
              catchError(() => of({ id: counterpartId, name: `Account ${counterpartId}` }))
            )
          )
        );
      }),
      tap((accounts) => {
        accounts.forEach((acct) => {
          this.accountNames.set(acct.id, acct.name);
        });
      })
    ).subscribe({
      next: () => {
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
    return tx.fromAccountId === this.viewAccountId;
  }

  isReceived(tx: TransactionResponse) {
    return tx.toAccountId === this.viewAccountId;
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

  getContactName(tx: TransactionResponse) {
    const accountId = this.isSent(tx) ? tx.toAccountId : tx.fromAccountId;
    return this.accountNames.get(accountId) ?? `Account ${accountId}`;
  }

  badgeClass(status: string) {
    return status === 'SUCCESS' ? 'badge success' : 'badge failed';
  }
}