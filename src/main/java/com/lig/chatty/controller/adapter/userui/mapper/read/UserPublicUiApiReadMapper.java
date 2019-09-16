package com.lig.chatty.controller.adapter.userui.mapper.read;

import com.lig.chatty.controller.adapter.userui.dto.UserPublicDto;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapper;
import com.lig.chatty.controller.core.uiadapter.mapper.read.GenericUiApiReadMapperConfig;
import com.lig.chatty.domain.User;
import org.mapstruct.Mapper;

@Mapper(config = GenericUiApiReadMapperConfig.class)
public interface UserPublicUiApiReadMapper extends GenericUiApiReadMapper<User, UserPublicDto> {
}


