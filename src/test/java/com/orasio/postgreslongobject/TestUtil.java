package com.orasio.postgreslongobject;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by spielerl on 24/11/2017.
 */
public class TestUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestUtil.class);
    private static final String ANGRY_BIRDS_FILE_NAME = "ANGRY_BIRDS_FILE_NAME";
    private static final String URL = "http://localhost:9999/rest/files/"+ANGRY_BIRDS_FILE_NAME ;

    public interface MeasureTimeCallback<T>{
        T call();
    }
    private RestTemplate restTemplate;

    public <T> T doWithMeasureTime(MeasureTimeCallback<T> measureTimeCallback, String action){
        LocalTime start = LocalTime.now();
        T result = measureTimeCallback.call();
        LocalTime end = LocalTime.now();
        Duration duration = Duration.between ( start, end );
        logger.warn(action+" took "+duration.getSeconds()+" seconds");
        return result;
    }

    public RestTemplate getRestTemplate(){
        if(restTemplate==null){
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add( new ByteArrayHttpMessageConverter());
        }
        return restTemplate;
    }


    public void uploadFile(){
        //read file
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new ClassPathResource("angry_birds_space.apk"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);




        ResponseEntity<Void> result = doWithMeasureTime(
                ()->getRestTemplate().exchange(URL, HttpMethod.POST, entity, Void.class),
                "Upload file");

        Assert.assertTrue(result.getStatusCode() == HttpStatus.OK);

    }
    public void downloadFile(String message){
        // Optional Accept header
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));


        // Streams the response instead of loading it all in memory
        ResponseExtractor<Void> responseExtractor = response -> {
            // Here I write the response to a file but do what you like
            Files.copy(response.getBody(), Paths.get(UUID.randomUUID().toString()+"_copyAngryBirds.apk"));
            return null;
        };
//		restTemplate.execute(URI.create("www.something.com"), HttpMethod.GET, requestCallback, responseExtractor);

        doWithMeasureTime(
                ()-> getRestTemplate().execute(URI.create(URL),HttpMethod.GET, requestCallback, responseExtractor),
                message);
    }
}
