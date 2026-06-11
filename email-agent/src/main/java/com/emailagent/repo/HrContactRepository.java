package com.emailagent.repo;

import com.emailagent.model.HrContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrContactRepository extends JpaRepository<HrContact, Long> {

}