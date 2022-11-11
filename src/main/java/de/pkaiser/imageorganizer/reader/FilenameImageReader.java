package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.pkaiser.imageorganizer.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilenameImageReader implements ImageReader {

	private List<FilenameImagePattern> patterns;
	
	public FilenameImageReader(FilenameImagePattern... patterns) {
		this.patterns = Arrays.asList(patterns);
	}	
	
	@Override
	public Image read(File file) throws Exception {
		for (FilenameImagePattern pattern : patterns) {
			final Optional<Instant> time = pattern.tryParse(file.getName());
			if (time.isPresent()) {
				return new Image(file, time.get());
			}
		}
		
		log.warn("Unknown image pattern: {}", file.toPath());
		
		return null;
	}

}
