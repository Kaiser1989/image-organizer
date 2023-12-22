package de.pkaiser.imageorganizer;

import de.pkaiser.imageorganizer.archive.MediaFileVisitor;
import de.pkaiser.imageorganizer.meta.MetaData;
import de.pkaiser.imageorganizer.meta.MetaData.Entity;
import de.pkaiser.imageorganizer.meta.MetaDataRestorer;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MetadataService {

  private final MetaData metaData;

  private final Settings settings;

  public void restoreMetadata() throws IOException {
    log.info("Start restoring metadata ...");

    this.metaData.getFiles().clear();

    // first of all, restore all meta data
    final MetaDataRestorer restorer = new MetaDataRestorer();
    new MediaFileVisitor(settings.getFolder()).run(path -> {
      this.metaData.add(Entity.builder().path(path).date(restorer.restore(path)).build());
    });

    // mark metadata as initialized
    this.metaData.markInitialized();

    log.info("... metadata restored successfully!");
  }
}
