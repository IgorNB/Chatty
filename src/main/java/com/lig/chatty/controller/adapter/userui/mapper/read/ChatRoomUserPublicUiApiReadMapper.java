package com.lig.chatty.controller.adapter.userui.mapper.read;

import com.lig.chatty.controller.adapter.userui.dto.ChatRoomUserPublicDto;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapper;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapperConfig;
import com.lig.chatty.domain.ChatRoomUser;
import org.mapstruct.Mapper;

@Mapper(config = GenericUiApiReadMapperConfig.class)
public interface ChatRoomUserPublicUiApiReadMapper extends GenericUiApiReadMapper<ChatRoomUser, ChatRoomUserPublicDto> {
}


