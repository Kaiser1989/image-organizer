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

  public Instant restore(final Path path) {
    Optional<Instant> optCreationDate;

    // check if we already have a creation date
    if ((optCreationDate = READER.read(path)).isPresent()) {
      return optCreationDate.get();
    }

    // get creation date from somewhere else
    final String source;
    if ((optCreationDate = READER.readFromFilename(path)).isPresent()) {
      source = "file name (" + path.getFileName() + ")";
    } else {
      optCreationDate = READER.readFromAttributes(path);
      source = "attributes";
    }

    // should not happen
    if (optCreationDate.isEmpty()) {
      throw new RuntimeException("Failed to fetch creation date from file: " + path.getFileName());
    }

    // update images
    if (Imaging.hasImageFileExtension(path.toFile())) {
      log.info("Restore creation date from {}", source);
      WRITER.update(path, optCreationDate.get());
    }

    return optCreationDate.get();
  }
}
