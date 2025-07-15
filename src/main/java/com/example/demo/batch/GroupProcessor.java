package com.example.demo.batch;

import com.example.demo.domain.*;
import com.example.demo.dto.GroupJson;
import com.example.demo.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupProcessor implements ItemProcessor<GroupJson, Group> {

    private final UserRepository userRepo;

    @Override
    public Group process(GroupJson j) {
        Group g = new Group();
        g.setGroupName(j.groupName());
        g.setGroupDescription(j.groupDescription());
        g.setActive(j.active());

        if (j.users() != null) {
            j.users().forEach(uRef ->
                g.getUsers().add(userRepo.findById(uRef.userName())
                                   .orElseGet(() -> userRepo.save(new User() {{
                                       setUserName(uRef.userName());
                                       setEmailAddress(uRef.emailAddress());
                                       setFirstName(uRef.name());
                                       setActive(uRef.active());
                                   }}))));
        }
        return g;
    }
}
