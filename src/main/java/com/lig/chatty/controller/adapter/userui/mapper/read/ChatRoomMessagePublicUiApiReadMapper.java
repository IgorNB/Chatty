package com.lig.chatty.controller.adapter.userui.mapper.read;

import com.lig.chatty.controller.adapter.userui.dto.ChatRoomMessagePublicDto;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapper;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapperConfig;
import com.lig.chatty.domain.ChatRoomMessage;
import org.mapstruct.Mapper;

@Mapper(config = GenericUiApiReadMapperConfig.class, uses = {UserPublicUiApiReadMapper.class})
public interface ChatRoomMessagePublicUiApiReadMapper extends GenericUiApiReadMapper<ChatRoomMessage, ChatRoomMessagePublicDto> {
}


