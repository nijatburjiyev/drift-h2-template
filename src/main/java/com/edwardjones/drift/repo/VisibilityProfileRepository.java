package com.edwardjones.drift.repo;

import com.edwardjones.drift.domain.VisibilityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisibilityProfileRepository extends JpaRepository<VisibilityProfile, String> {
}
