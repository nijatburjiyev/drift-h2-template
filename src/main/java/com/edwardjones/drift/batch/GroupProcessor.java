package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.Group;
import com.edwardjones.drift.domain.User;
import com.edwardjones.drift.dto.GroupJson;
import com.edwardjones.drift.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GroupProcessor implements ItemProcessor<GroupJson, Group> {

    private final UserRepository userRepository;

    @Override
    public Group process(GroupJson json) {
        Group group = new Group();
        group.setGroupName(json.groupName());
        group.setGroupDescription(json.groupDescription());
        group.setActive(json.active());

        // Handle user relationships - find existing users and link them
        if (json.users() != null && !json.users().isEmpty()) {
            Set<User> users = new HashSet<>();
            for (GroupJson.UserRef userRef : json.users()) {
                userRepository.findById(userRef.userName())
                    .ifPresent(users::add);
            }
            group.setUsers(users);
        }

        return group;
    }
}
