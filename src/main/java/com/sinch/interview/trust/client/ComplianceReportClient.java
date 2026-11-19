package com.sinch.interview.trust.client;

public interface ComplianceReportClient {
    void sendBlockReport(String projectId, String reason);
}
