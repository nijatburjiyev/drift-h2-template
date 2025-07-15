package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.SubmissionType;

public interface SubmissionTypeRepository extends JpaRepository<SubmissionType, Long> {}
