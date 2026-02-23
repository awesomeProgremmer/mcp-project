import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout, retry, map } from 'rxjs/operators';
import { environment } from '../environment';
import { QueryRequest, QueryResult } from '../query.model';


@Injectable({
  providedIn: 'root'
})
export class QueryService {

  // ─── Single Place for all URLs ────────────
  private readonly apiUrl = environment.apiUrl;
  private readonly queryEndpoint = environment.endpoints.query;
  private readonly healthEndpoint = environment.endpoints.health;
  private readonly TIMEOUT_MS = 30000;

  constructor(private http: HttpClient) {}

 submitQuery(request: QueryRequest): Observable<QueryResult> {
  const url = `${this.apiUrl}${this.queryEndpoint}`;
  console.log('Calling URL:', url);
  console.log('Payload:', request);

  return this.http.post(
    url,
    request,
    { responseType: 'text' }
  ).pipe(
    timeout(this.TIMEOUT_MS),
    retry({ count: 1, delay: 1000 }),
    map((response: string) => ({
  naturalLanguageQuery: request.query,
  generatedSql: response
    .replace(/```sql/g, '')   // remove ```sql
    .replace(/```/g, '')      // remove ```
    .trim(),                  // remove whitespace
  executionTimeMs: 0,
  timestamp: new Date().toISOString(),
  rowsAffected: 0,
  data: [],
  table: request.table,
  columns: request.columns
})),
    catchError(this.handleError)
  );
}

  healthCheck(): Observable<any> {
    return this.http.get(
      `${this.apiUrl}${this.healthEndpoint}`
    ).pipe(
      timeout(5000),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse | any): Observable<never> {
    let message: string;

    if (error.status === 0) {
      message = 'Cannot reach the server. Is Spring Boot running?';
    } else if (error.name === 'TimeoutError' || error.status === 408) {
      message = 'Request timed out. Ollama may still be generating — try again.';
    } else if (error.status === 400) {
      message = error.error?.message || 'Invalid query. Please rephrase.';
    } else if (error.status === 500) {
      message = error.error?.message || 'Server error. Check Spring Boot logs.';
    } else {
      message = `Unexpected error (${error.status}): ${error.message}`;
    }

    return throwError(() => new Error(message));
  }
}