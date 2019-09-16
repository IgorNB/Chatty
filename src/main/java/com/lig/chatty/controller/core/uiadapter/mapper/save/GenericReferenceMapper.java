package com.lig.chatty.controller.core.uiadapter.mapper.save;

import com.lig.chatty.controller.core.uiadapter.dto.ReferencePublicDto;
import com.lig.chatty.domain.core.PersistentObject;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GenericReferenceMapper {
    //If Entity has field with other entity - we skip generating mapping for it. Instead - we call this method.
    <E extends PersistentObject> E getReferenceHavingOnlyId(PersistentObject dto, @TargetType Class<E> entityClass);

    <E extends PersistentObject> E getReferenceHavingOnlyId2(ReferencePublicDto<E> dto, @TargetType Class<E> entityClass);

    //If Entity has field with List of other entities - we skip generating "updating" mapping for List. Instead - we call this method.
    default <E extends PersistentObject, D extends PersistentObject> List<E> ignoreListUpdate(List<D> dtoList, @MappingTarget List<E> toBeUpdatedEntityList) {
        return toBeUpdatedEntityList;
    }

    // If Entity has field with List of other entities - we skip generating "creating" mapping for List. Instead - we call this method.
    // Here null need to be returned, so we skip Sonar warning "Empty arrays and collections should be returned instead of null"
    @SuppressWarnings("squid:S1168")
    default <E extends PersistentObject, D extends PersistentObject> List<E> ignoreListCreate(List<D> dtoList) {
        return null;
    }
}
