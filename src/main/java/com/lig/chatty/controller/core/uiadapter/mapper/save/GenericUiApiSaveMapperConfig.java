package com.lig.chatty.controller.core.uiadapter.mapper.save;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.WARN, collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE, componentModel = "spring", uses = {GenericReferenceMapper.class})
public interface GenericUiApiSaveMapperConfig {
}
