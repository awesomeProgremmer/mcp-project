export interface QueryResult {
  naturalLanguageQuery: string;
  generatedSql: string;
  executionTimeMs: number;
  timestamp: string;
  rowsAffected?: number;
  data?: any[];
  table: string;        // ← ADD
  columns: string[];    // ← ADD
}
export interface QueryHistoryItem {
  id: string;
  query: string;
  sql: string;
  timestamp: Date;
  table: string;        // ← ADD
  columns: string[]; 
  success: boolean;
  error?: string;
}

export interface QueryRequest {
  query: string;
  table: string;
  columns: string[];
  description: string;
}