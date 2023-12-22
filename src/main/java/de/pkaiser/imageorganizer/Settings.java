package de.pkaiser.imageorganizer;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class Settings {

	@Value("${app.folder}")
	private String folder;

	@Value("${app.images:}")
	private List<String> images;

	@Value("${app.videos:}")
	private List<String> videos;
}
