package com.orasio.postgreslongobject.controller;

import com.orasio.postgreslongobject.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by spielerl on 20/11/2017.
 */
@RestController
@RequestMapping("/rest/files/")
public class FileController {
    @Autowired
    FileService fileService;

    @RequestMapping(method = RequestMethod.GET, value = "/{fileName}")
    public StreamingResponseBody download(@PathVariable String fileName)     throws FileNotFoundException {
        return (os) ->  fileService.downloadFile(fileName, os);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/{fileName}")
    public void addSource(@RequestParam MultipartFile file,@PathVariable String fileName) throws IOException, SQLException {
        fileService.create(fileName, file.getInputStream());
    }

}
