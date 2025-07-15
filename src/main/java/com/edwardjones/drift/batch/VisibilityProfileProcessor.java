package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.*;
import com.edwardjones.drift.dto.VisibilityProfileJson;
import com.edwardjones.drift.repo.GroupRepository;
import com.edwardjones.drift.repo.SubmissionTypeRepository;
import com.edwardjones.drift.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class VisibilityProfileProcessor implements ItemProcessor<VisibilityProfileJson, VisibilityProfile> {

    private final GroupRepository groupRepository;
    private final SubmissionTypeRepository submissionTypeRepository;
    private final UserRepository userRepository;

    @Override
    public VisibilityProfile process(VisibilityProfileJson json) {
        VisibilityProfile vp = new VisibilityProfile();
        vp.setName(json.name());
        vp.setCanOnlyViewInvolvedSubmissions(json.canOnlyViewInvolvedSubmissions());
        vp.setCanViewAllSubmitterGroups(json.canViewAllSubmitterGroups());
        vp.setCanViewAllSubmissionTypes(json.canViewAllSubmissionTypes());
        vp.setCanSubmitOnBehalfOfSelf(json.canSubmitOnBehalfOfSelf());
        vp.setActive(json.active());

        // Handle on behalf of groups
        if (json.onBehalfOfGroups() != null) {
            Set<VpGroupLink> onBehalfOfGroups = new HashSet<>();
            for (VisibilityProfileJson.GroupWithUsers groupWithUsers : json.onBehalfOfGroups()) {
                VpGroupLink link = new VpGroupLink();
                link.setVisibilityProfileName(vp.getName());
                link.setGroupName(groupWithUsers.groupName());
                link.setLinkType("ON_BEHALF_OF");
                link.setActive(groupWithUsers.active());
                link.setVisibilityProfile(vp);
                onBehalfOfGroups.add(link);
            }
            vp.setOnBehalfOfGroups(onBehalfOfGroups);
        }

        // Handle can view submissions for groups
        if (json.canViewSubmissionsForGroups() != null) {
            Set<VpGroupLink> canViewGroups = new HashSet<>();
            for (VisibilityProfileJson.GroupWithUsers groupWithUsers : json.canViewSubmissionsForGroups()) {
                VpGroupLink link = new VpGroupLink();
                link.setVisibilityProfileName(vp.getName());
                link.setGroupName(groupWithUsers.groupName());
                link.setLinkType("CAN_VIEW");
                link.setActive(groupWithUsers.active());
                link.setVisibilityProfile(vp);
                canViewGroups.add(link);
            }
            vp.setCanViewSubmissionsForGroups(canViewGroups);
        }

        // Handle can view submission types
        if (json.canViewSubmissionTypes() != null) {
            Set<SubmissionType> submissionTypes = new HashSet<>();
            for (VisibilityProfileJson.SubmissionTypeRef typeRef : json.canViewSubmissionTypes()) {
                submissionTypeRepository.findById(typeRef.id())
                    .ifPresent(submissionTypes::add);
            }
            vp.setCanViewSubmissionTypes(submissionTypes);
        }

        // Set audit fields
        vp.setJsonChecksum(ChecksumUtil.sha256Hex(json));

        return vp;
    }
}
