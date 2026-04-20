package com.selloLegitimo.GestionPreElectoral.grpc;

import com.selloLegitimo.grpc.elecciones.EleccionServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcEleccionClientConfig implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(GrpcEleccionClientConfig.class);

	@Value("${grpc.elecciones.host}")
	private String host;

	@Value("${grpc.elecciones.port}")
	private int port;

	private ManagedChannel channel;

	@Bean
	public EleccionServiceGrpc.EleccionServiceBlockingStub eleccionServiceStub() {
		channel = ManagedChannelBuilder.forAddress(host, port)
				.usePlaintext()
				.build();
		logger.info("Canal gRPC creado hacia {}:{}", host, port);
		return EleccionServiceGrpc.newBlockingStub(channel);
	}

	@Override
	public void destroy() {
		if (channel != null && !channel.isShutdown()) {
			logger.info("Cerrando canal gRPC...");
			channel.shutdown();
		}
	}
}
