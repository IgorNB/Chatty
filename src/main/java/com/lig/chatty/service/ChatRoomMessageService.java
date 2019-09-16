package com.lig.chatty.service;

import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomMessage;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRoomMessageService {
    @Transactional
    @NonNull Page<ChatRoomMessage> findAllByChatRoom(@NonNull ChatRoom chatRoom, Pageable pageable, @NonNull UserDetails userDetails);

    @Transactional
    @NonNull Integer getLastPageNumberByChatRoom(@NonNull ChatRoom chatRoom, @NonNull Integer pageSize, @NonNull UserDetails userDetails);

    @Transactional
    @NonNull ChatRoomMessage create(@NonNull ChatRoomMessage entity, @NonNull UserDetails userDetails);
}
