package com.lig.chatty.repository;

import com.lig.chatty.domain.*;
import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("springDataJpa")
public interface ChatRoomMessageRepository extends JpaRepository<ChatRoomMessage, String> {
    @NonNull Integer countChatRoomMessagesByChatRoom(@NonNull ChatRoom chatRoom);
    @NonNull Page<ChatRoomMessage> findAllByChatRoom(@NonNull ChatRoom chatRoom, Pageable pageable);
}