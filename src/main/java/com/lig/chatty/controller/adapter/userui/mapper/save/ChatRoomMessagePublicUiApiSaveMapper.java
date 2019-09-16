package com.lig.chatty.controller.adapter.userui.mapper.save;

import com.lig.chatty.controller.adapter.userui.dto.ChatRoomMessagePublicDto;
import com.lig.chatty.controller.core.uiadapter.mapper.save.GenericUiApiSaveMapper;
import com.lig.chatty.controller.core.uiadapter.mapper.save.GenericUiApiSaveMapperConfig;
import com.lig.chatty.domain.ChatRoomMessage;
import org.mapstruct.Mapper;

@Mapper(config = GenericUiApiSaveMapperConfig.class)
public interface ChatRoomMessagePublicUiApiSaveMapper extends GenericUiApiSaveMapper<ChatRoomMessage, ChatRoomMessagePublicDto> {
}


