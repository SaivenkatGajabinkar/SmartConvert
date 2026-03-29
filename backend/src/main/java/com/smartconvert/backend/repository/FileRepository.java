package com.smartconvert.backend.repository;

import com.smartconvert.backend.model.FileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, String> {
}
