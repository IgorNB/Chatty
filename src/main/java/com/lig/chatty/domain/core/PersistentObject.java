package com.lig.chatty.domain.core;

import java.io.Serializable;

public interface PersistentObject extends Serializable {
    String getId();

    void setId(String id);

    Integer getVersion();

    void setVersion(Integer id);
}
