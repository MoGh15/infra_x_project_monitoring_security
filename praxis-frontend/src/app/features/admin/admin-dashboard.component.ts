import { Component, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { AdminSubmissionsService } from '../../core/api/admin-submissions.service';
import { SubmissionListItem, SubmissionStatus } from '../../core/api/submission.model';
import { AuthService } from '../../core/auth/auth.service';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { MatMenuModule } from '@angular/material/menu';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { AdminSubmissionsSseService } from './admin-submissions-sse.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,

    MatToolbarModule,
    MatButtonModule,
    MatChipsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatIconModule,

    MatMenuModule,
    MatFormFieldModule,
    MatInputModule,

    MatPaginatorModule,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export class AdminDashboardComponent {
  displayedColumns = ['status', 'name', 'createdAt', 'birthDate', 'actions'];

  private api = inject(AdminSubmissionsService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private sse = inject(AdminSubmissionsSseService);

  loading = signal(true);
  error = signal<string | null>(null);

  filter = signal<SubmissionStatus | 'ALL'>('ALL');
  submissions = signal<SubmissionListItem[]>([]);

  // Name filter (persisted)
  private readonly NAME_FILTER_KEY = 'praxis_admin_name_filter';
  nameFilter = signal<string>(localStorage.getItem(this.NAME_FILTER_KEY) ?? '');

  // ✅ Pagination (persisted)
  pageSizeOptions = [5, 10, 20, 50];
  private readonly PAGE_SIZE_KEY = 'praxis_admin_page_size';
  private readonly PAGE_INDEX_KEY = 'praxis_admin_page_index';

  pageSize = signal<number>(Number(localStorage.getItem(this.PAGE_SIZE_KEY) ?? 10));
  pageIndex = signal<number>(Number(localStorage.getItem(this.PAGE_INDEX_KEY) ?? 0));

  constructor() {
    effect(() => localStorage.setItem(this.NAME_FILTER_KEY, this.nameFilter()));
    effect(() => localStorage.setItem(this.PAGE_SIZE_KEY, String(this.pageSize())));
    effect(() => localStorage.setItem(this.PAGE_INDEX_KEY, String(this.pageIndex())));

    // Wenn Filter sich ändern: auf Seite 1 springen
    effect(() => {
      // dependency tracking
      this.filter();
      this.nameFilter();
      // reset
      this.pageIndex.set(0);
    });

    this.load();

    this.sse.connect().subscribe(ev => {
      if (ev.type === 'created') {
        const current = this.submissions();
        const exists = current.some(x => x.id === ev.payload.id);
        if (!exists) {
          this.submissions.set(this.sortByCreatedDesc([ev.payload, ...current]));
        }
      }

      if (ev.type === 'updated') {
        const current = this.submissions();
        const next = current.map(x => x.id === ev.payload.id ? { ...x, ...ev.payload } : x);
        this.submissions.set(this.sortByCreatedDesc(next));
      }
    });
  }

  filtered = computed(() => {
    const status = this.filter();
    const q = (this.nameFilter() ?? '').trim().toLowerCase();

    let list = this.submissions();

    if (status !== 'ALL') list = list.filter(s => s.status === status);

    if (!q) return list;

    return list.filter(s => this.displayName(s).toLowerCase().includes(q));
  });

  totalCount = computed(() => this.filtered().length);

  // ✅ nur die aktuelle Seite
  paged = computed(() => {
    const list = this.filtered();
    const size = this.pageSize();
    const idx = this.pageIndex();

    const start = idx * size;
    const end = start + size;

    // falls pageIndex "zu weit" ist (z.B. nach Filterwechsel), clampen
    if (start >= list.length && idx > 0) {
      this.pageIndex.set(0);
      return list.slice(0, size);
    }

    return list.slice(start, end);
  });

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
  }

  load() {
    this.loading.set(true);
    this.error.set(null);

    this.api.list().subscribe({
      next: (items) => {
        this.submissions.set(this.sortByCreatedDesc(items));
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Konnte Submissions nicht laden.');
        this.loading.set(false);
      },
    });
  }

  setFilter(v: SubmissionStatus | 'ALL') {
    this.filter.set(v);
  }

  setNameFilter(v: string) {
    this.nameFilter.set(v ?? '');
  }

  clearNameFilter(ev?: MouseEvent) {
    ev?.stopPropagation();
    this.nameFilter.set('');
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  displayName(s: SubmissionListItem) {
    const fn = s.patientData?.firstName ?? '';
    const ln = s.patientData?.lastName ?? '';
    const full = `${fn} ${ln}`.trim();
    return full || '(ohne Name)';
  }

  displayBirthDate(val?: string | null): string {
    if (!val) return '-';
    if (/^\d{2}\.\d{2}\.\d{4}$/.test(val)) return val;

    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(val);
    if (m) return `${m[3]}.${m[2]}.${m[1]}`;

    return val;
  }

  private sortByCreatedDesc(items: SubmissionListItem[]): SubmissionListItem[] {
    return [...items].sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));
  }
}
