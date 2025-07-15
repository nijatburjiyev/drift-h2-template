package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.SubmissionType;
import com.edwardjones.drift.dto.SubmissionTypeJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SubmissionTypeProcessor implements ItemProcessor<SubmissionTypeJson, SubmissionType> {

    @Override
    public SubmissionType process(SubmissionTypeJson json) {
        SubmissionType submissionType = new SubmissionType();
        submissionType.setId(json.id());
        submissionType.setName(json.name());
        submissionType.setDescription(json.description());
        submissionType.setHelpText(json.helpText());
        submissionType.setActive(json.active());
        submissionType.setInitialState(json.initialState());
        submissionType.setPriority(json.priority());

        // Handle approved states collection
        if (json.approvedStates() != null) {
            submissionType.setApprovedStates(new ArrayList<>(json.approvedStates()));
        }

        // Handle formats collection
        if (json.formats() != null) {
            submissionType.setFormats(new ArrayList<>(json.formats()));
        }

        // Set audit fields
        submissionType.setJsonChecksum(ChecksumUtil.sha256Hex(json));

        return submissionType;
    }
}
