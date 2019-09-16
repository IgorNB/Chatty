package com.lig.chatty.config.websocketbroker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
@Profile("simpleWebSocketBroker")
public class SimpleWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue/", "/topic/");
		registry.setApplicationDestinationPrefixes("/chatroom");
		registry.configureBrokerChannel().taskExecutor().corePoolSize(300).maxPoolSize(300).queueCapacity(3000).keepAliveSeconds(900);
	}

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer()
	{
		ServletServerContainerFactoryBean container = new  ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192);
		container.setMaxBinaryMessageBufferSize(8192);
		container.setAsyncSendTimeout(5000L);
		container.setMaxSessionIdleTimeout(600000L);
		return container;
	}

	@Bean(name={"messageBrokerTaskScheduler", "messageBrokerSockJsTaskScheduler"})
	public ThreadPoolTaskScheduler messageBrokerTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("MessageBrokerOLOLO-");
		scheduler.setPoolSize(60/*Runtime.getRuntime().availableProcessors()*/);
		scheduler.setRemoveOnCancelPolicy(true);
		return scheduler;
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration)
	{
		registration.taskExecutor().corePoolSize(100).maxPoolSize(100);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration)
	{
		registration.taskExecutor().corePoolSize(100).maxPoolSize(100);
	}
}
