package com.edwardjones.drift.repo;

import com.edwardjones.drift.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, String> {
}
