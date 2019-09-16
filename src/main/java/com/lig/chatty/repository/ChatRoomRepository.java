package com.lig.chatty.repository;

import com.lig.chatty.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("springDataJpa")
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
}