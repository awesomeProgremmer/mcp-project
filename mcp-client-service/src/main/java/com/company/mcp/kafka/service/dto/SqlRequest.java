package com.company.mcp.kafka.service.dto;

import java.util.List;

public class SqlRequest {

    private String requestId;
    private String table;
    private List<String> columns;
    private String description;

    // ─── Getters & Setters ───────────────────────────────────────────
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}