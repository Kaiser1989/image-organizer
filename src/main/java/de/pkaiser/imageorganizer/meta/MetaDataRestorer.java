package de.pkaiser.imageorganizer.meta;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.Imaging;

@Slf4j
@AllArgsConstructor
public class MetaDataRestorer {

	private static final MetaDataReader READER = new MetaDataReader();

	private static final MetaDataWriter WRITER = new MetaDataWriter();

	public Optional<Instant> restore(final Path path) {
		Optional<Instant> optCreationDate;

		// check if we already have a creation date
		if ((optCreationDate = READER.read(path)).isPresent()) {
			return optCreationDate;
		}

		// get creation date from somewhere else
		if ((optCreationDate = READER.readFromFilename(path)).isPresent()) {
			log.info("Restore creation date from file name: {}", path.getFileName());
		} else {
			optCreationDate = READER.readFromAttributes(path);
			log.info("Restore creation date from attributes");
		}

		// update this file to
		WRITER.update(path, optCreationDate.get());
		return optCreationDate;
	}
}
