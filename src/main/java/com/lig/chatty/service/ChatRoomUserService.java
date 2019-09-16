package com.lig.chatty.service;

import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomUser;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRoomUserService {
    @Transactional
    @NonNull Page<ChatRoomUser> findAllByChatRoom(@NonNull ChatRoom chatRoom, @NonNull Pageable pageable, @NonNull UserDetails userDetails);
}
