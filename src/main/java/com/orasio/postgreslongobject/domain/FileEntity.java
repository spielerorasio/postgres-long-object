package com.orasio.postgreslongobject.domain;

/**
 * Created by spielerl on 18/11/2017.
 */

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import java.sql.Blob;

@Entity
@Table(name = "FILE_ENTITY" )
public class FileEntity extends AbstractPersistable<Long> {
    @Version //optimistic lock
    private long version;

    @Column(name = "FILE_NAME")
    private String fileName;
//    @Column(name = "CONTENT_TYPE")
//    private String contentType;
    @Column(name = "SIZE")
    private long size;

    @Lob
    @Column(name = "FILE_DATA")
    private Blob fileData;

    public long getVersion() {
        return version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

//    public String getContentType() {
//        return contentType;
//    }
//
//    public void setContentType(String contentType) {
//        this.contentType = contentType;
//    }
//
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Blob getFileData() {
        return fileData;
    }

    public void setFileData(Blob fileData) {
        this.fileData = fileData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileEntity that = (FileEntity) o;

        return getFileName().equals(that.getFileName());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getFileName().hashCode();
        return result;
    }
}