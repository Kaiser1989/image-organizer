package de.pkaiser.imageorganizer;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class Application {

	public static void main(String[] args) throws BeansException, Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		ctx.getBean(ImageService.class).organize();
		
		// close again
		log.info("Closing Application");
		ctx.close();
	}
}
