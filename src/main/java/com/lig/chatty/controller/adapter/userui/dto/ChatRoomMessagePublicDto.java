package com.lig.chatty.controller.adapter.userui.dto;

import com.lig.chatty.domain.core.PersistentObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
@Getter
@Setter
@ToString(callSuper = true)
public class ChatRoomMessagePublicDto implements PersistentObject {

    private String id;

    private Integer version;

    private String message;

    private UserPublicDto author;
}