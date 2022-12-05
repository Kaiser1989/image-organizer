package de.pkaiser.imageorganizer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class Settings {

	@Value("${archivePath}")
	private String path;
}
