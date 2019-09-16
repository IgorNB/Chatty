package com.lig.chatty.repository.common;

import org.springframework.transaction.annotation.Transactional;

public interface EntityFactory<T> {
    @Transactional
    T getNewEntityInstance();
}
