package com.orasio.postgreslongobject;

import com.orasio.postgreslongobject.service.FileService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConcurrencyTest {

	private static final String FILE_NAME = "FILE_NAME";
	private static final String ANGRY_BIRDS_APK = "target/test-classes/angry_birds_space.apk";
	private static final Path ANGRY_BIRDS_PATH = Paths.get(ANGRY_BIRDS_APK);
	private InputStream angry_birds_input_stream ;


	@Autowired
	FileService fileService;

	@Before
	public void init() throws IOException {
		Assert.assertNotNull(fileService);
//		angry_birds_input_stream = Files.newInputStream(ANGRY_BIRDS_PATH);
//		Assert.assertTrue(angry_birds_input_stream.available()>0);

	}


	@Test
	public void concurrencyTest() throws IOException, SQLException, InterruptedException {
		int concurrentThreads = 10;
		AtomicInteger index = new AtomicInteger(0);
		for (int j = 0; j < 10; j++) {
		ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreads);
		CountDownLatch countDownLatch = new CountDownLatch(concurrentThreads);
		long timeMillis = System.currentTimeMillis();
		for (int i = 0; i < concurrentThreads; i++) {
			executorService.execute(() -> {
						try {
							InputStream angry_birds_input_stream = Files.newInputStream(ANGRY_BIRDS_PATH);
							Assert.assertTrue(angry_birds_input_stream.available()>0);
							String fileName = FILE_NAME + index.incrementAndGet();
							fileService.create(fileName, angry_birds_input_stream);
							Assert.assertTrue(fileService.exists(fileName));

						} catch (IOException | SQLException e) {
							e.printStackTrace();
						}  finally {
							countDownLatch.countDown();
						}
					}
			);

		}
		countDownLatch.await();
		int seconds = (int) ((System.currentTimeMillis()-timeMillis) / 1000);
		System.out.println(concurrentThreads+ " concurrent file upload took  : "+seconds+ " seconds  =======================================");

		}

	}

}
