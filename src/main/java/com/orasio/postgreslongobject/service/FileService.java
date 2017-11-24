package com.orasio.postgreslongobject.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * Created by spielerl on 18/11/2017.
 */
public interface FileService {
    boolean exists(String fileName);
    InputStream loadFileAsInputStream(String fileName);
    void downloadFile(String fileName, OutputStream outputStream);
    void create(String fileName, InputStream inputStream) throws IOException, SQLException;
    void update(String fileName, InputStream inputStream) throws IOException, SQLException;
    boolean deleteFile(String fileName);
}
