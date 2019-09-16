package com.lig.chatty.repository;
import com.lig.chatty.domain.User;

import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

@Profile("springDataJpa")
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(@NonNull String email);
}
