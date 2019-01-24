package com.handson.broker.service.producer.activemq;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.handson.broker.service.activemq.EmbeddedActivemqBroker;

/**
 * @author sveera
 *
 */
public class ActiveMQProducerServiceTest {

	private final String brokerURL = "failover://(tcp://localhost:61616)?initialReconnectDelay=2000";
	private final String embeddedBrokerUrl = "tcp://localhost:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600&amp;wireFormat.maxInactivityDuration=120000";
	private ActiveMQProducerService activeMQProducerService;
	private EmbeddedActivemqBroker embeddedActivemqBroker;

	@Before
	public void setUp() throws Exception {
		embeddedActivemqBroker = new EmbeddedActivemqBroker(embeddedBrokerUrl);
		embeddedActivemqBroker.startService();
		activeMQProducerService = new ActiveMQProducerService(brokerURL);

	}

	@After
	public void tearDown() {
		activeMQProducerService.stopService();
		embeddedActivemqBroker.stopService();
	}

	@Test
	public void testActiveMQProducerService() {
		Assert.assertNotNull("activeMQProducerService Cannot be null", activeMQProducerService);
	}

	@Test
	public void testActiveMQProducerServiceGetName() {
		assertEquals("Expected ActiveMQProducerService name and actual name are not same", "activemq",
				activeMQProducerService.getName());
	}

	@Test
	public void testPublishData() throws InterruptedException {
		for (int deviceMessageIndex = 0; deviceMessageIndex < 10000; deviceMessageIndex++)
			activeMQProducerService.publishData("activeMQTestQueue",
					"This device data generated  @ " + new Date().toString());
		waitForProducerToFinish();
	}

	private void waitForProducerToFinish() throws InterruptedException {
		Thread.sleep(3000);
	}

}
