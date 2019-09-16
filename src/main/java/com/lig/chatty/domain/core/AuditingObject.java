package com.lig.chatty.domain.core;

public interface AuditingObject<U> {
    U getCreatedBy();

    Long getCreatedDate();

    U getLastUpdBy();

    Long getUpdatedDate();
}
