package com.lig.chatty.controller.core.uiadapter.mapper.read;

import com.lig.chatty.domain.core.PersistentObject;
import org.springframework.data.domain.Page;

public interface GenericUiApiReadMapper<E extends PersistentObject, D extends PersistentObject> {

    D entityToDto(E entity);

    default Page<D> pageableEntityToDto(Page<E> page) {
        return page.map(this::entityToDto);
    }
}


