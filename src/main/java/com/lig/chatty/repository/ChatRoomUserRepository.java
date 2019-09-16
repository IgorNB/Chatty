package com.lig.chatty.repository;

import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomUser;
import com.lig.chatty.domain.User;
import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Profile("springDataJpa")
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, String> {
    @NonNull Page<ChatRoomUser> findAllByChatRoom(@NonNull ChatRoom chatRoom, Pageable pageable);
    @NonNull Optional<ChatRoomUser> findByChatRoomAndUser(@NonNull ChatRoom chatRoom, @NonNull User user);
}