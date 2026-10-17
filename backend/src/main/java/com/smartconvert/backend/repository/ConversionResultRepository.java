package com.smartconvert.backend.repository;

import com.smartconvert.backend.model.ConversionResultEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ConversionResultRepository - Data Access Layer for conversion results.
 * 
 * Spring Data Derived Query Methods:
 * - Spring Data parses method names following naming conventions ("findBy<Property><Condition>")
 *   and automatically generates the SQL queries at runtime:
 *   1. findBySourceFileId: Generates "WHERE source_file_id = ?" to find results for a specific uploaded file.
 *   2. findByConvertedAtBefore: Generates "WHERE converted_at < ?" used by the cleanup scheduler
 *      to purge conversion records older than 10 minutes.
 */
@Repository
public interface ConversionResultRepository extends CrudRepository<ConversionResultEntity, String> {

    /**
     * Finds all conversion outputs generated from a specific source file.
     */
    List<ConversionResultEntity> findBySourceFileId(String sourceFileId);

    /**
     * Finds all conversion results created before a specific timestamp.
     * Used by StorageService's scheduled cleanup task.
     */
    List<ConversionResultEntity> findByConvertedAtBefore(LocalDateTime time);
}

