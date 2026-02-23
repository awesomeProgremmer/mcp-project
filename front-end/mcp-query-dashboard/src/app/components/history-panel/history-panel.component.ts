import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { QueryHistoryItem, QueryRequest } from '../../query.model';


@Component({
  selector: 'app-history-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './history-panel.component.html',
  styleUrl: './history-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HistoryPanelComponent {

  @Input() history: QueryHistoryItem[] = [];
  @Output() replayQuery = new EventEmitter<QueryRequest>();
  @Output() clearHistory = new EventEmitter<void>();

  expandedId: string | null = null;

  toggleExpand(id: string): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  replay(item: QueryHistoryItem): void {
    this.replayQuery.emit({
      query: item.query,
      table: item.table,
      columns: item.columns,
      description: item.query
    });
  }

  trackById(_index: number, item: QueryHistoryItem): string {
    return item.id;
  }
}