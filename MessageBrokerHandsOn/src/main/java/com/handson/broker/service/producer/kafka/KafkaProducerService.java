package com.handson.broker.service.producer.kafka;

import static com.handson.broker.utility.UtilityLogger.logExceptionStackTrace;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;

import com.handson.broker.service.producer.BrokerProducerService;

/**
 * @author sveera
 *
 */
public class KafkaProducerService<K, V> implements BrokerProducerService<V> {
	private final Logger logger = getLogger(getClass());

	private static final String MAX_WAIT_TIME_FOR_CONNECTION = "5000";
	private final KafkaProducerCallbackHandler kafkaProducerCallbackHandler = new KafkaProducerCallbackHandler();
	private final String kafkaBootStrapServers;
	private final String keySerializerClass;
	private final String valueSerializerClass;
	private final String producerClientId;
	private KafkaProducer<K, V> kafkaProducer;

	public KafkaProducerService(String kafkaBootStrapServers, String keySerializerClass, String valueSerializerClass,
			String producerClientId) {
		super();
		this.kafkaBootStrapServers = kafkaBootStrapServers;
		this.keySerializerClass = keySerializerClass;
		this.valueSerializerClass = valueSerializerClass;
		this.producerClientId = producerClientId;
	}

	@Override
	public String getName() {
		return "kafka";
	}

	public void startService() {
		tryInstantiatingKafkaProducer();
	}

	public void stopService() {
		kafkaProducer.close();
	}

	public void publishData(String topicName, V value) {
		tryPublishingMessage(topicName, value);
	}

	private void tryPublishingMessage(String topicName, V value) {
		try {
			if (kafkaProducer == null)
				tryInstantiatingKafkaProducer();
			ProducerRecord<K, V> producerRecord = new ProducerRecord<K, V>(topicName, value);
			kafkaProducer.send(producerRecord, kafkaProducerCallbackHandler);
		} catch (Throwable th) {
			logExceptionStackTrace(th, getClass());
		}
	}

	private void tryInstantiatingKafkaProducer() {
		try {
			initKafkaProducer();
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate " + this.getClass().getSimpleName());
		}
	}

	private void initKafkaProducer() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", kafkaBootStrapServers);
		properties.setProperty("max.block.ms", MAX_WAIT_TIME_FOR_CONNECTION);
		properties.setProperty("client.id", producerClientId);
		final Serializer<K> keySerializer = keySerializerClass == null || keySerializerClass.isEmpty()
				? (Serializer<K>) new StringSerializer()
				: (Serializer<K>) Class.forName(keySerializerClass).newInstance();
		final Serializer<V> valueSerializer = valueSerializerClass == null || valueSerializerClass.isEmpty()
				? (Serializer<V>) new StringSerializer()
				: (Serializer<V>) Class.forName(valueSerializerClass).newInstance();
		kafkaProducer = new KafkaProducer<>(properties, keySerializer, valueSerializer);
		logger.trace("Kafka Producer Instance been created " + kafkaProducer);
	}

	private final class KafkaProducerCallbackHandler implements Callback {
		@Override
		public void onCompletion(RecordMetadata metadata, Exception exception) {
			handleForSuccess(metadata);
			handleException(exception);
		}

		private void handleForSuccess(RecordMetadata metadata) {
			if (metadata != null)
				logger.trace("Data Published to broker successfully " + metadata);
		}

		private void handleException(Exception exception) {
			if (exception != null) {
				logger.error("Failed to publish the record to kafka");
				logExceptionStackTrace(exception, getClass());
			}
		}
	}
}
