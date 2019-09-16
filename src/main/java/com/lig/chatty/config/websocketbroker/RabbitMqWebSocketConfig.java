package com.lig.chatty.config.websocketbroker;

import com.lig.chatty.security.config.AppPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
@Profile("rabbitMqWebSocketBroker")
public class RabbitMqWebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final String relayHost;
	private final Integer relayPort;

	@Autowired
	public RabbitMqWebSocketConfig(AppPropertiesConfig appPropertiesConfig) {
		this.relayHost = appPropertiesConfig.getRelay().getHost();
		this.relayPort = appPropertiesConfig.getRelay().getPort();
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableStompBrokerRelay("/queue/", "/topic/")
			.setUserDestinationBroadcast("/topic/unresolved.user.dest")
			.setUserRegistryBroadcast("/topic/registry.broadcast")
			.setRelayHost(relayHost)
			.setRelayPort(relayPort);

		registry.setApplicationDestinationPrefixes("/chatroom");
		registry.configureBrokerChannel().taskExecutor().corePoolSize(40).maxPoolSize(60).queueCapacity(3000).keepAliveSeconds(900);
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
		scheduler.setPoolSize(20);
		scheduler.setRemoveOnCancelPolicy(true);
		return scheduler;
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration)
	{
		registration.taskExecutor().corePoolSize(40).maxPoolSize(60);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration)
	{
		registration.taskExecutor().corePoolSize(40).maxPoolSize(60);
	}
}
