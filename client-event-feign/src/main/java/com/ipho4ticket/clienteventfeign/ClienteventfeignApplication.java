package com.ipho4ticket.clienteventfeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClienteventfeignApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClienteventfeignApplication.class, args);
	}

}
