package com.example.chat.ops.governance.service.application;

import com.example.chat.ops.governance.service.domain.AuditEntry;
import com.example.chat.ops.governance.service.domain.PolicyProposal;
import com.example.chat.ops.governance.service.presentation.dto.CreatePolicyProposalRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class GovernanceService {
    private final List<PolicyProposal> proposals = new CopyOnWriteArrayList<>();
    private final List<AuditEntry> audits = new CopyOnWriteArrayList<>();

    public PolicyProposal createProposal(String actor, CreatePolicyProposalRequest request) {
        PolicyProposal proposal = new PolicyProposal(
                UUID.randomUUID().toString(),
                request.projectId(),
                request.title(),
                request.description(),
                request.prUrl(),
                actor,
                Instant.now()
        );
        proposals.add(proposal);
        audits.add(new AuditEntry(actor, "policy.proposal.created", request.projectId(), proposal.proposalId(), Instant.now()));
        return proposal;
    }

    public List<AuditEntry> audits() {
        return new ArrayList<>(audits);
    }
}
