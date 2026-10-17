package com.smartconvert.backend.repository;

import com.smartconvert.backend.model.FileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * FileRepository - Data Access Layer for uploaded files.
 * 
 * Spring Data JPA Repository Pattern:
 * - By extending CrudRepository<FileEntity, String>, Spring Data automatically generates
 *   all standard CRUD operations (save, findById, findAll, delete, etc.) at runtime.
 * - No manual SQL queries or DAO boilerplate code required!
 * - @Repository marks this interface as a Spring Data access bean and enables
 *   automatic exception translation into Spring's DataAccessException hierarchy.
 */
@Repository
public interface FileRepository extends CrudRepository<FileEntity, String> {
}

