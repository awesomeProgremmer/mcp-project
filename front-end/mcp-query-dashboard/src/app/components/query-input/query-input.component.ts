import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { QueryRequest } from '../../query.model';

@Component({
  selector: 'app-query-input',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './query-input.component.html',
  styleUrl: './query-input.component.scss'
})
export class QueryInputComponent implements OnInit, OnDestroy {

  @Input() isLoading: boolean = false;
 @Output() querySubmitted = new EventEmitter<QueryRequest>();

  queryControl = new FormControl('', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(500)
  ]);

  tableControl = new FormControl('', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(100)
  ]);
  newColumnControl = new FormControl('', [
    Validators.minLength(1),
    Validators.maxLength(100)
  ]);

  columns: string[] = [];

  addColumn(): void {
    const col = this.newColumnControl.value?.trim();
    if(col && !this.columns.includes(col)) {
      this.columns.push(col);
      this.newColumnControl.reset();
    }
  }
  removeColumn(index: number): void {
    this.columns.splice(index, 1);
  }

  charCount: number = 0;
  private destroy$ = new Subject<void>();

  suggestions: string[] = [
    'Show all users created this month',
    'Count orders by status',
    'Find top 10 products by revenue',
    'List customers with no recent activity'
  ];

  ngOnInit(): void {
    this.queryControl.valueChanges.pipe(
      debounceTime(100),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(value => {
      this.charCount = value?.length || 0;
    });
  }

onSubmit(): void {
  if (this.queryControl.valid &&
      this.tableControl.valid &&
      this.columns.length > 0 &&
      !this.isLoading) {
    this.querySubmitted.emit({
      query: this.queryControl.value || '',
      table: this.tableControl.value || '',
      columns: this.columns,
      description: this.queryControl.value || ''
    });
  }
}

  useSuggestion(suggestion: string): void {
    this.queryControl.setValue(suggestion);
    this.onSubmit();
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSubmit();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}