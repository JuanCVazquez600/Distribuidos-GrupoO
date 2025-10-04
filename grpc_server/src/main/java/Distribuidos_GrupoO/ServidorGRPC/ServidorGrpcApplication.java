package Distribuidos_GrupoO.ServidorGRPC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"Distribuidos_GrupoO.ServidorGRPC", "com.grpc"})
public class ServidorGrpcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorGrpcApplication.class, args);
	}

}
