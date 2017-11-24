package com.orasio.postgreslongobject.util;

import com.jakewharton.disklrucache.DiskLruCache;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class FileCacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileCacheUtil.class);

    private DiskLruCache fileCache;

    @Value("${file.cache.size.mb:300}")
    private int cacheSizeInMB;

    @Value("${file.cache.temp.location:./}")
    private String cacheLocation;


    private static final int MAX_CACHE_NAME_LENGTH = 64;


    @PostConstruct
    public void init() {
        try {
            if (cacheSizeInMB > 0) {
                File directory = new File(System.getProperty("java.io.tmpdir")+this.cacheLocation);
                fileCache = DiskLruCache.open(directory, 1, 1, cacheSizeInMB * 1024 * 1024);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize file cache.", e);
        }
    }

    public File cacheFile(String pathToFile, File file) {
        // if we don't have the cache initialized, just return the file
        if (fileCache != null && pathToFile != null) {
            try {
                // Put the downloaded file into cache for future use.
                String cacheName = this.convertToCacheName(pathToFile);
                DiskLruCache.Editor editor = null;
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    editor = fileCache.edit(cacheName);
                    OutputStream outputStream = editor.newOutputStream(0);
                    try {
                        IOUtils.copy(inputStream, outputStream);
                    } finally {
                        // Limitation of DiskLruCache: The output stream has to be closed before commit to the editor, otherwise the commit will not take effect.
                        IOUtils.closeQuietly(outputStream);
                    }
                    editor.commit();
                } catch (Exception e) {
                    if (editor != null) {
                        editor.abort();
                    }
                    logger.error("Failed to edit cache file \"" + cacheName + "\".", e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;

    }


    public FileInputStream getFileInputStreamFromCache(String pathToFile) {
        if (fileCache == null || pathToFile == null) {
            return null;
        }
        String cacheName = this.convertToCacheName(pathToFile);
        logger.info("Cache name is: {}", cacheName);
        try {
            DiskLruCache.Snapshot snapshot = fileCache.get(cacheName);
            if (snapshot != null) {
                return (FileInputStream) snapshot.getInputStream(0);
            }
            logger.info("Failed to get file '{}' from cache.", cacheName);
        } catch (IOException e) {
            logger.error("Failed to get file '" + cacheName + "' from cache.", e);
        }
        return null;
    }

    public FileInputStream cacheInputStream(String pathToFile, InputStream inputStream) {
        // if we don't have the cache initialized, just return the file
        if (fileCache != null && pathToFile != null) {
            try {
                // Put the downloaded file into cache for future use.
                String cacheName = this.convertToCacheName(pathToFile);
                DiskLruCache.Editor editor = null;
                try {
                    editor = fileCache.edit(cacheName);
                    OutputStream outputStream = editor.newOutputStream(0);
                    try {
                        IOUtils.copy(inputStream, outputStream);
                    } finally {
                        // Limitation of DiskLruCache: The output stream has to be closed before commit to the editor, otherwise the commit will not take effect.
                        IOUtils.closeQuietly(outputStream);
                    }
                    editor.commit();
                } catch (Exception e) {
                    if (editor != null) {
                        editor.abort();
                    }
                    logger.error("Failed to edit cache file \"" + cacheName + "\".", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
                DiskLruCache.Snapshot snapshot = fileCache.get(cacheName);
                return (FileInputStream) snapshot.getInputStream(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    final String convertToCacheName(String name) {
        String[] fragments = name.split("/");
        String cacheName = fragments[fragments.length - 1];
        // Limitation of DiskLruCache: File name can only be "[a-z0-9_-]{1,64}".
        cacheName = (cacheName.length() > MAX_CACHE_NAME_LENGTH ? cacheName.substring(0, MAX_CACHE_NAME_LENGTH) : cacheName)
                .toLowerCase()
                .replaceAll("[^a-z0-9_-]","");
        return cacheName;
    }

    final DiskLruCache getFileCache() {
        return this.fileCache;
    }

    public boolean removeFileFromCache(String pathToFile) throws IOException {
        if (pathToFile != null) {
            String cacheName = convertToCacheName(pathToFile);
            return getFileCache().remove(cacheName);
        }

        return false;
    }

    public boolean isFileExistInCache(String pathToFile) {
        if (pathToFile != null) {
            String cacheName = convertToCacheName(pathToFile);
            try (DiskLruCache.Snapshot snapshot = getFileCache().get(cacheName)) {
                if (snapshot != null) {
                    return true;
                }
            } catch (IOException e) {
                logger.error("fail to search for the file "+pathToFile+"in the cache ", e);
                return false;
            }
        }

        return false;
    }
}
