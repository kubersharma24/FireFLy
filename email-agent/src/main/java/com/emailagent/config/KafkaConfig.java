package com.emailagent.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.emailagent.model.EmailRequest;

@Configuration
public class KafkaConfig {
	@Bean
	public ProducerFactory<String, String> stringProducerFactory() {
	    Map<String, Object> config = new HashMap<>();

	    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

	    return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, String> stringKafkaTemplate() {
	    return new KafkaTemplate<>(stringProducerFactory());
	}
	
	@Bean
	public ProducerFactory<String, EmailRequest> emailProducerFactory() {
	    Map<String, Object> config = new HashMap<>();

	    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

	    return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, EmailRequest> emailKafkaTemplate() {
	    return new KafkaTemplate<>(emailProducerFactory());
	}
	
	@Bean
	public ConsumerFactory<String, String> stringConsumerFactory() {

	    Map<String, Object> props = new HashMap<>();

	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "health-group");

	    return new DefaultKafkaConsumerFactory<>(
	            props,
	            new StringDeserializer(),
	            new StringDeserializer()
	    );
	}
	
	@Bean
	public ConsumerFactory<String, EmailRequest> emailConsumerFactory() {

	    Map<String, Object> props = new HashMap<>();

	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "email-group");

	    JsonDeserializer<EmailRequest> deserializer =
	            new JsonDeserializer<>(EmailRequest.class);

	    deserializer.addTrustedPackages("*");
	    deserializer.setUseTypeHeaders(false);

	    return new DefaultKafkaConsumerFactory<>(
	            props,
	            new StringDeserializer(),
	            deserializer
	    );
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String>
	stringKafkaListenerContainerFactory() {

	    ConcurrentKafkaListenerContainerFactory<String, String> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();

	    factory.setConsumerFactory(stringConsumerFactory());

	    return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, EmailRequest>
	emailKafkaListenerContainerFactory() {

	    ConcurrentKafkaListenerContainerFactory<String, EmailRequest> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();

	    factory.setConsumerFactory(emailConsumerFactory());

	    return factory;
	}

}
