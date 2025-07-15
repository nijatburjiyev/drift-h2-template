package com.example.demo.batch;

import com.example.demo.domain.*;
import com.example.demo.dto.VisibilityProfileJson;
import com.example.demo.repo.GroupRepository;
import com.example.demo.repo.SubmissionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisibilityProfileProcessor
        implements ItemProcessor<VisibilityProfileJson, VisibilityProfile> {

    private final GroupRepository groupRepo;
    private final SubmissionTypeRepository typeRepo;

    @Override
    public VisibilityProfile process(VisibilityProfileJson j) {
        VisibilityProfile vp = new VisibilityProfile();
        vp.setName(j.name());
        vp.setCanOnlyViewInvolvedSubmissions(j.canOnlyViewInvolvedSubmissions());
        vp.setCanViewAllSubmitterGroups(j.canViewAllSubmitterGroups());
        vp.setCanViewAllSubmissionTypes(j.canViewAllSubmissionTypes());
        vp.setCanSubmitOnBehalfOfSelf(j.canSubmitOnBehalfOfSelf());
        vp.setActive(j.active());

        if (j.canViewSubmissionTypes() != null) {
            j.canViewSubmissionTypes().forEach(stj -> {
                SubmissionType st = typeRepo.findById(stj.id())
                                   .orElseGet(() -> typeRepo.save(toEntity(stj)));
                vp.getSpecificSubmissionTypes().add(st);
            });
        }

        if (j.onBehalfOfGroups() != null) {
            j.onBehalfOfGroups().forEach(gj ->
                vp.getGroupLinks().add(new VpGroupLink(
                    new VpGroupLink.VpGroupId(vp.getName(), gj.groupName()),
                    vp,
                    groupRepo.findById(gj.groupName())
                            .orElseGet(() -> groupRepo.save(new Group() {{
                                setGroupName(gj.groupName());
                                setActive(gj.active());
                            }})),
                    VpGroupLink.Role.ON_BEHALF_OF,
                    gj.active()
                )));
        }

        if (j.canViewSubmissionsForGroups() != null) {
            j.canViewSubmissionsForGroups().forEach(gj ->
                vp.getGroupLinks().add(new VpGroupLink(
                    new VpGroupLink.VpGroupId(vp.getName(), gj.groupName()),
                    vp,
                    groupRepo.findById(gj.groupName())
                            .orElseGet(() -> groupRepo.save(new Group() {{
                                setGroupName(gj.groupName());
                                setActive(gj.active());
                            }})),
                    VpGroupLink.Role.CAN_VIEW,
                    gj.active()
                )));
        }

        return vp;
    }

    private SubmissionType toEntity(com.example.demo.dto.SubmissionTypeJson j) {
        SubmissionType st = new SubmissionType();
        st.setId(j.id());
        st.setName(j.name());
        st.setActive(j.active());
        st.setInitialState(j.initialState());
        st.setPriority(j.priority());
        st.getApprovedStates().addAll(j.approvedStates());
        st.getFormats().addAll(j.formats());
        st.setDescription(j.description());
        st.setHelpText(j.helpText());
        return st;
    }
}
