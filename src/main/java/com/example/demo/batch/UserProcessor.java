package com.example.demo.batch;

import com.example.demo.domain.*;
import com.example.demo.dto.UserJson;
import com.example.demo.repo.GroupRepository;
import com.example.demo.repo.VisibilityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProcessor implements ItemProcessor<UserJson, User> {

    private final GroupRepository groupRepo;
    private final VisibilityProfileRepository vpRepo;

    @Override
    public User process(UserJson j) {
        User u = new User();
        u.setUserName(j.userName());
        u.setFirstName(j.firstName());
        u.setLastName(j.lastName());
        u.setEmailAddress(j.emailAddress());
        u.setActive(j.active());
        u.setTimeZone(j.timeZone());
        u.setLocale(j.locale());

        if (j.groups() != null) {
            j.groups().forEach(gref ->
                u.getGroups().add(groupRepo.findById(gref.groupName())
                                           .orElseGet(() -> groupRepo.save(new Group() {{
                                               setGroupName(gref.groupName());
                                               setActive(gref.active());
                                           }}))));
        }

        if (j.visibilityProfile() != null) {
            vpRepo.findById(j.visibilityProfile()).ifPresent(u::setVisibilityProfile);
        }

        return u;
    }
}
