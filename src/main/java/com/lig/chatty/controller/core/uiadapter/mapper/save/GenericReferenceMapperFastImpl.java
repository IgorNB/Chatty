package com.lig.chatty.controller.core.uiadapter.mapper.save;

import com.lig.chatty.controller.core.uiadapter.dto.ReferencePublicDto;
import com.lig.chatty.domain.core.PersistentObject;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class GenericReferenceMapperFastImpl implements GenericReferenceMapper {

    final
    EntityManager entityManager;

    @Autowired
    public GenericReferenceMapperFastImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <E extends PersistentObject> E getReferenceHavingOnlyId(PersistentObject dto, @TargetType Class<E> entityClass) {
        return dto != null ? entityManager.getReference(entityClass, dto.getId()) : null;
    }

    @Override
    public <E extends PersistentObject> E getReferenceHavingOnlyId2(ReferencePublicDto<E> dto, @TargetType Class<E> entityClass) {
        return getReferenceHavingOnlyId(dto, entityClass);
    }





}