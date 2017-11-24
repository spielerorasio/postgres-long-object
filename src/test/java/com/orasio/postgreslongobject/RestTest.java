package com.orasio.postgreslongobject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class RestTest {

	@Autowired
	TestUtil testUtil;


	@Before
	public void init() throws IOException {
		Assert.assertNotNull(testUtil);
//		server.getEmbeddedServletContainer().
	}


	@Test
	public void testCreateAndLoadFile() throws IOException, SQLException, InterruptedException {
		testUtil.uploadFile();
		testUtil.downloadFile("1st Download file");
		testUtil.downloadFile("2nd Download file");
	}

	@Test
	public void concurrencyTest() throws IOException, SQLException, InterruptedException {
		testUtil.uploadFile();
		int concurrentThreads = 30;
		AtomicInteger index = new AtomicInteger(0);
		final ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreads);
		CountDownLatch countDownLatch = new CountDownLatch(concurrentThreads);
		testUtil.doWithMeasureTime(
				()->{

					for (int i = 0; i < concurrentThreads; i++) {
						executorService.execute(() ->
								{
									testUtil.downloadFile(index.incrementAndGet()+" Download file");
									countDownLatch.countDown();
								}
						);

					}
					try {
						countDownLatch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;

				}
				, " concurrent file download");

	}



}
