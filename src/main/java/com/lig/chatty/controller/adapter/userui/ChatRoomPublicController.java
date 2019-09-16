package com.lig.chatty.controller.adapter.userui;

import com.lig.chatty.controller.adapter.userui.dto.ChatRoomMessagePublicDto;
import com.lig.chatty.controller.adapter.userui.dto.ChatRoomUserPublicDto;
import com.lig.chatty.controller.adapter.userui.mapper.read.ChatRoomMessagePublicUiApiReadMapper;
import com.lig.chatty.controller.adapter.userui.mapper.read.ChatRoomUserPublicUiApiReadMapper;
import com.lig.chatty.controller.adapter.userui.mapper.save.ChatRoomMessagePublicUiApiSaveMapper;
import com.lig.chatty.domain.Authority;
import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomMessage;
import com.lig.chatty.domain.User;
import com.lig.chatty.domain.core.AbstractPersistentObject;
import com.lig.chatty.domain.core.GenericAbstractPersistentAuditingObject;
import com.lig.chatty.security.config.AppPropertiesConfig;
import com.lig.chatty.service.ChatRoomMessageService;
import com.lig.chatty.service.ChatRoomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import java.util.Optional;

@RestController
public class ChatRoomPublicController {

    public static final String CHAT_ROOM_API = "api/v1/chatRooms";
    public static final String CHAT_ROOM_ID_PATH_VARIABLE = "chatRoomId";
    public static final String CHAT_ROOM_MESSAGES_API = CHAT_ROOM_API + "/{" + CHAT_ROOM_ID_PATH_VARIABLE + "}/messages/";
    public static final String CHAT_ROOM_USERS_API = CHAT_ROOM_API + "/{" + CHAT_ROOM_ID_PATH_VARIABLE + "}/members/";
    public static final String PAGE_NUMBER_PARAM = "pageNumber";


    private final EntityManager entityManager;
    private final ChatRoomMessageService chatRoomMessageService;
    private final ChatRoomUserService chatRoomUserService;

    private final ChatRoomMessagePublicUiApiReadMapper chatRoomMessageReadMapper;
    private final ChatRoomMessagePublicUiApiSaveMapper chatRoomMessageSaveMapper;
    private final ChatRoomUserPublicUiApiReadMapper chatRoomUserReadMapper;

    private AppPropertiesConfig appPropertiesConfig;


    @Autowired
    public ChatRoomPublicController(EntityManager entityManager, ChatRoomMessageService chatRoomMessageService, ChatRoomUserService chatRoomUserService, ChatRoomMessagePublicUiApiReadMapper chatRoomMessageReadMapper, ChatRoomMessagePublicUiApiSaveMapper chatRoomMessageSaveMapper, ChatRoomUserPublicUiApiReadMapper chatRoomUserReadMapper, AppPropertiesConfig appPropertiesConfig) {
        this.entityManager = entityManager;
        this.chatRoomMessageReadMapper = chatRoomMessageReadMapper;
        this.chatRoomMessageSaveMapper = chatRoomMessageSaveMapper;
        this.chatRoomMessageService = chatRoomMessageService;
        this.chatRoomUserService = chatRoomUserService;
        this.chatRoomUserReadMapper = chatRoomUserReadMapper;
        this.appPropertiesConfig = appPropertiesConfig;
    }


    @RolesAllowed({Authority.Roles.USER, Authority.Roles.ADMIN})
    @GetMapping(value = CHAT_ROOM_MESSAGES_API)
    public Page<ChatRoomMessagePublicDto> findAllMessages(
            @PathVariable(name = CHAT_ROOM_ID_PATH_VARIABLE) String chatRoomId,
            @RequestParam(name = PAGE_NUMBER_PARAM, required = false) Integer pageNumber,
            @ApiIgnore Authentication authentication) {
        Integer pageSize = appPropertiesConfig.getRoomMessage().getPageSize();
        ChatRoom chatRoom = entityManager.getReference(ChatRoom.class, chatRoomId);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        pageNumber = Optional.ofNullable(pageNumber).orElseGet(() -> chatRoomMessageService.getLastPageNumberByChatRoom(chatRoom, pageSize, userDetails));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort
                .by(Sort.Direction.ASC, GenericAbstractPersistentAuditingObject.Fields.createdDate)
                .and(Sort.by(Sort.Direction.DESC, AbstractPersistentObject.Fields.id))
        );
        return chatRoomMessageReadMapper.pageableEntityToDto(chatRoomMessageService.findAllByChatRoom(chatRoom, pageable, userDetails));
    }

    @PostMapping(value = CHAT_ROOM_MESSAGES_API)
    @RolesAllowed({Authority.Roles.USER, Authority.Roles.ADMIN})
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomMessagePublicDto createMessage(
            @PathVariable(name = CHAT_ROOM_ID_PATH_VARIABLE) String chatRoomId,
            @RequestBody ChatRoomMessagePublicDto dto,
            @ApiIgnore Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        ChatRoomMessage entity = chatRoomMessageSaveMapper.dtoToCreateEntity(dto);

        entity.setAuthor(entityManager.getReference(User.class, userDetails.getUsername()));
        entity.setChatRoom(entityManager.getReference(ChatRoom.class, chatRoomId));

        return chatRoomMessageReadMapper.entityToDto(chatRoomMessageService.create(entity, userDetails));
    }

    @RolesAllowed({Authority.Roles.USER, Authority.Roles.ADMIN})
    @GetMapping(value = CHAT_ROOM_USERS_API)
    public Page<ChatRoomUserPublicDto> findAllUsers(
            @PathVariable(name = CHAT_ROOM_ID_PATH_VARIABLE) String chatRoomId,
            @RequestParam(name = PAGE_NUMBER_PARAM, required = false) Integer pageNumber,
            @ApiIgnore Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ChatRoom chatRoom = entityManager.getReference(ChatRoom.class, chatRoomId);
        Integer pageSize = appPropertiesConfig.getRoomMember().getPageSize();
        pageNumber = Optional.ofNullable(pageNumber).orElseGet(() -> 0);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort
                .by(Sort.Direction.ASC, GenericAbstractPersistentAuditingObject.Fields.createdDate)
                .and(Sort.by(Sort.Direction.DESC, AbstractPersistentObject.Fields.id))
        );
        return chatRoomUserReadMapper.pageableEntityToDto(chatRoomUserService.findAllByChatRoom(chatRoom, pageable, userDetails));
    }

}

