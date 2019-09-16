package com.lig.chatty.controller.adapter.userui.dto;

import com.lig.chatty.controller.core.uiadapter.dto.ReferencePublicDto;
import com.lig.chatty.domain.User;
import com.lig.chatty.domain.core.PersistentObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
@Getter
@Setter
@ToString(callSuper = true)
public class ChatRoomUserPublicDto implements PersistentObject {

    private String id;
    private Integer version;

    private ReferencePublicDto<User> user;
}