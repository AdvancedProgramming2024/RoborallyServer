package dtu.compute.RoborallyServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.System.out;

@SpringBootApplication
public class RoborallyServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoborallyServerApplication.class, args);
		out.print("Success");
	}
}
