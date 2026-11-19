package com.sinch.interview.trust.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubDownstreamClients implements GatewayCacheClient, ComplianceReportClient {

    private static final Logger log = LoggerFactory.getLogger(StubDownstreamClients.class);

    @Override
    public void invalidateProject(String projectId) {
        log.info("[StubGatewayCacheClient] Invalidated gateway cache for projectId={}", projectId);
    }

    @Override
    public void sendBlockReport(String projectId, String reason) {
        log.info("[StubComplianceReportClient] Sent fraud report for projectId={} reason={}", projectId, reason);
    }
}
