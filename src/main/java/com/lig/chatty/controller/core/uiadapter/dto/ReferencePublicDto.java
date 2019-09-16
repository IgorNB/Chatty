package com.lig.chatty.controller.core.uiadapter.dto;

import com.lig.chatty.domain.core.PersistentObject;
import lombok.Getter;
import lombok.Setter;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;

@NotThreadSafe
@Getter
@Setter
public class ReferencePublicDto<E extends PersistentObject> implements PersistentObject {
    @NotNull
    private String id;

    private Integer version;
}