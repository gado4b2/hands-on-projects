package com.handson.broker.service.consumer.handler;

/**
 * @author sveera
 *
 */
public interface BrokerConsumerDataHandler<T> {

	void handleConsumerMessage(T consumerData);
}
