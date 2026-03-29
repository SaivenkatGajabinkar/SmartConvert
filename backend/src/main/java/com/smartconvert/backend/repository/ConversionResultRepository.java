package com.smartconvert.backend.repository;

import com.smartconvert.backend.model.ConversionResultEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversionResultRepository extends CrudRepository<ConversionResultEntity, String> {
    List<ConversionResultEntity> findBySourceFileId(String sourceFileId);
    List<ConversionResultEntity> findByConvertedAtBefore(LocalDateTime time);
}
