package com.orasio.postgreslongobject.service;

import com.orasio.postgreslongobject.domain.FileEntity;
import com.orasio.postgreslongobject.domain.FileEntityRepository;
import com.orasio.postgreslongobject.util.FileCacheUtil;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * Created by spielerl on 18/11/2017.
 */
@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    FileEntityRepository fileEntityRepository;
    @Autowired
    FileCacheUtil fileCacheUtil;


    @Autowired
    EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String fileName) {
        FileEntity fileEntity = fileEntityRepository.findByFileName(fileName);
        if (fileEntity != null) {
            return true;
        }
        return false;
    }


    @Override
    @Transactional(readOnly = true)
    public InputStream loadFileAsInputStream(String fileName) {
        if(fileCacheUtil.isFileExistInCache(fileName)){
            return  fileCacheUtil.getFileInputStreamFromCache(fileName);
        }
        FileEntity fileEntity = fileEntityRepository.findByFileName(fileName);
        if (fileEntity != null) {
            try {
                InputStream binaryStream = fileEntity.getFileData().getBinaryStream();
                FileInputStream fileInputStream = fileCacheUtil.cacheInputStream(fileName, binaryStream);
                return fileInputStream;
            } catch (SQLException e) {
                logger.error("error loading file "+fileName, e);
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public void downloadFile(String fileName, OutputStream outputStream) {
        InputStream inputStream = loadFileAsInputStream(fileName);
        if (inputStream != null) {
            try {
                IOUtils.copy(inputStream, outputStream);
            }  catch (IOException e) {
                logger.error("error downloading file "+fileName, e);
            }  finally {
                try {
                    inputStream.close();
                } catch (IOException e) { }
                try {
                    outputStream.flush();
                } catch (IOException e) { }
            }
        }
    }




    @Override
    @Transactional
    public void create(String fileName, InputStream inputStream) throws IOException, SQLException {
        if(exists(fileName) ){
            return;
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setSize(inputStream.available());
        streamFile(fileEntity, inputStream);

    }

    @Override
    @Transactional
    public void update(String fileName, InputStream inputStream) throws IOException, SQLException {
        FileEntity fileEntity = fileEntityRepository.findByFileName(fileName);
        fileEntity.setSize(inputStream.available());
        streamFile(fileEntity, inputStream);
    }

    @Override
    @Transactional
    public boolean deleteFile(String fileName) {
        return fileEntityRepository.deleteByFileName(fileName) > 0L;
    }

    FileEntity streamFile(FileEntity fileEntity, InputStream inputStream) throws IOException, SQLException {
        Blob blob = null;
        try {
            blob = Hibernate.getLobCreator((Session)entityManager.getDelegate())
                    .createBlob(inputStream, inputStream.available());
            fileEntity.setFileData(blob);
            return fileEntityRepository.saveAndFlush(fileEntity);
        } finally {
            if(blob != null){
                blob.free();
            }
        }
    }

//    public FileEntity streamFile(FileEntity fileEntity, String filePath) throws IOException, SQLException {
//        Blob blob = null;
//        try {
//            blob = createBlob(filePath);
//            fileEntity.setFileData(blob);
//            return fileEntityRepository.saveAndFlush(fileEntity);
//        }finally{
//            if(blob != null){
//                blob.free();
//            }
//        }
//    }

    private Blob createBlob(String filePath) throws IOException {
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        return Hibernate.getLobCreator((Session)entityManager.getDelegate())
                .createBlob(inputStream, file.length());
    }

}
