package Distribuidos_GrupoO.ServidorGRPC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@ComponentScan(basePackages = {"Distribuidos_GrupoO.ServidorGRPC", "com.grpc"})
public class ServidorGrpcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorGrpcApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
