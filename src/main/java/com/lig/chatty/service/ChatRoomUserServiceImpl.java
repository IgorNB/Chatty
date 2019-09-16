package com.lig.chatty.service;

import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomUser;
import com.lig.chatty.repository.ChatRoomUserRepository;

import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@ThreadSafe
@Service
public class ChatRoomUserServiceImpl implements ChatRoomUserService {
    private final ChatRoomUserRepository chatRoomUserRepository;

    @Autowired
    public ChatRoomUserServiceImpl(@NonNull ChatRoomUserRepository chatRoomUserRepository) {
        this.chatRoomUserRepository = chatRoomUserRepository;
    }

    @Override
    public @NonNull Page<ChatRoomUser> findAllByChatRoom(@NonNull ChatRoom chatRoom, @NonNull Pageable pageable, @NonNull UserDetails userDetails) {
        return chatRoomUserRepository.findAllByChatRoom(chatRoom, pageable);
    }
}
