package de.pkaiser.imageorganizer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "paths")
public class ImageSettings {
	
	private String origin;
	
	private String target;
}
