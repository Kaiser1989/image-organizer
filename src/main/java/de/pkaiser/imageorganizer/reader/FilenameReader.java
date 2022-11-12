package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.pkaiser.imageorganizer.DatedMediaFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilenameReader implements DatedMediaFileReader {

	private List<FilenamePattern> patterns;
	
	public FilenameReader(FilenamePattern... patterns) {
		this.patterns = Arrays.asList(patterns);
	}	
	
	@Override
	public DatedMediaFile read(File file) throws Exception {
		for (FilenamePattern pattern : patterns) {
			final Optional<Instant> time = pattern.tryParse(file.getName());
			if (time.isPresent()) {
				return new DatedMediaFile(file, time.get());
			}
		}
		
		log.warn("Unknown pattern: {}", file.toPath());
		
		return null;
	}

}
