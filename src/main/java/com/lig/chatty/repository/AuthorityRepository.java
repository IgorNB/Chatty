package com.lig.chatty.repository;

import com.lig.chatty.domain.Authority;
import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Profile("springDataJpa")
public interface AuthorityRepository extends JpaRepository<Authority, String> {

    @Transactional
    Authority getAuthorityByName(@NonNull String name);
}