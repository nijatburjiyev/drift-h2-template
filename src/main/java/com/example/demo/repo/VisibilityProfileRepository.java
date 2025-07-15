package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.VisibilityProfile;

public interface VisibilityProfileRepository extends JpaRepository<VisibilityProfile, String> {}
