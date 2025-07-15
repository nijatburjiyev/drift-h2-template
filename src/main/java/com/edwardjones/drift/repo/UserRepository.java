package com.edwardjones.drift.repo;

import com.edwardjones.drift.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
