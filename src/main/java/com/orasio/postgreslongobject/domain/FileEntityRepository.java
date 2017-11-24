package com.orasio.postgreslongobject.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by spielerl on 18/11/2017.
 */
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findByFileName(String fileName);
    int deleteByFileName(String fileName);
}
