import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil, switchMap, finalize } from 'rxjs/operators';
import { QueryService } from './service/query.service';
import { QueryInputComponent } from './components/query-input/query-input.component';
import { ResultDisplayComponent } from './components/result-display/result-display.component';
import { HistoryPanelComponent } from './components/history-panel/history-panel.component';
import { QueryHistoryItem, QueryRequest, QueryResult } from './query.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    QueryInputComponent,
    ResultDisplayComponent,
    HistoryPanelComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {

  // ─── State ────────────────────────────────
  isLoading = false;
  currentResult: QueryResult | null = null;
  error: string | null = null;
  queryHistory: QueryHistoryItem[] = [];

  // ─── RxJS ─────────────────────────────────
  private querySubject$ = new Subject<QueryRequest>();
  private destroy$ = new Subject<void>();

  constructor(private queryService: QueryService) {}

  ngOnInit(): void {
    this.loadHistory();
    this.setupQueryPipeline();
  }

  private setupQueryPipeline(): void {
    this.querySubject$.pipe(
      switchMap((request: QueryRequest) => {
        this.isLoading = true;
        this.error = null;
        this.currentResult = null;
        return this.queryService.submitQuery(request).pipe(
          finalize(() => this.isLoading = false)
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (result: QueryResult) => {
        this.currentResult = result;
        this.addToHistory(result);
      },
      error: (err) => {
        this.error = err?.message || 'Something went wrong.';
        this.isLoading = false;
      }
    });
  }

  // called by query-input component
  onQuerySubmit(request: QueryRequest): void {
    if (!request?.query?.trim()) return;
    this.querySubject$.next(request);
  }

  // called by history-panel component
  onReplayQuery(request: QueryRequest): void {
    this.querySubject$.next(request);
  }

  onClearHistory(): void {
    this.queryHistory = [];
    localStorage.removeItem('mcp_query_history');
  }

  private addToHistory(result: QueryResult): void {
    const item: QueryHistoryItem = {
      id: Date.now().toString(),
      query: result.naturalLanguageQuery,
      sql: result.generatedSql,
      table: result.table,
      columns: result.columns,
      timestamp: new Date(),
      success: true
    };
    this.queryHistory = [item, ...this.queryHistory].slice(0, 50);
    this.saveHistory();
  }

  private loadHistory(): void {
    try {
      const stored = localStorage.getItem('mcp_query_history');
      this.queryHistory = stored ? JSON.parse(stored) : [];
    } catch {
      this.queryHistory = [];
    }
  }

  private saveHistory(): void {
    try {
      localStorage.setItem('mcp_query_history', JSON.stringify(this.queryHistory));
    } catch { /* storage full */ }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}