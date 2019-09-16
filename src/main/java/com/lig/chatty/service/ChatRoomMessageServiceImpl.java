package com.lig.chatty.service;

import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomMessage;
import com.lig.chatty.repository.ChatRoomMessageRepository;

import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

@ThreadSafe
@Service
public class ChatRoomMessageServiceImpl implements ChatRoomMessageService {
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final SimpMessagingTemplate webSocketMessagingTemplate;

    @Autowired
    public ChatRoomMessageServiceImpl(@NonNull ChatRoomMessageRepository chatRoomMessageRepository, @NonNull SimpMessagingTemplate webSocketMessagingTemplate) {
        this.chatRoomMessageRepository = chatRoomMessageRepository;
        this.webSocketMessagingTemplate = webSocketMessagingTemplate;
    }

    public @NonNull Page<ChatRoomMessage> findAllByChatRoom(@NonNull ChatRoom chatRoom, Pageable pageable, @NonNull UserDetails userDetails) {
        return chatRoomMessageRepository.findAllByChatRoom(chatRoom, pageable);
    }

    public @NonNull Integer getLastPageNumberByChatRoom(@NonNull ChatRoom chatRoom, @NonNull Integer pageSize, @NonNull UserDetails userDetails) {
        Objects.requireNonNull(pageSize);
        long total = chatRoomMessageRepository.countChatRoomMessagesByChatRoom(chatRoom) ;
        int totalPages = pageSize == 0 ? 1 : (int) Math.ceil((double) total / (double) pageSize); //see org.springframework.data.domain.PageImpl.getTotalPages
        return totalPages == 0 ? 0 : totalPages -1;
    }

    @NonNull
    @Override
    public ChatRoomMessage create(@NonNull ChatRoomMessage entity, @NonNull UserDetails userDetails) {
        @NonNull ChatRoomMessage save = chatRoomMessageRepository.save(entity);
        webSocketMessagingTemplate.convertAndSend("/topic/" + entity.getChatRoom().getId() + ".public.messages", entity);
        return save;
    }
}
