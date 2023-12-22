package de.pkaiser.imageorganizer;

import de.pkaiser.imageorganizer.archive.Archiver;
import de.pkaiser.imageorganizer.meta.MetaData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizerService {

  private final Settings settings;

  private final MetaData metaData;

  public void temporalSplit() {
    log.info("Start splitting into temporal folders ...");

    // split files into years/month folders
    final Archiver archiver = new Archiver(settings.getFolder());
    metaData.getFiles().values().forEach(archiver::archive);
  }

  public void temporalMerge() {
    log.info("Start merging temporal folders ...");

    // split files into years/month folders
    final Archiver archiver = new Archiver(settings.getFolder());
    metaData.getFiles().values().forEach(archiver::archive);
  }

  public void cleanEmptyFolders() {
    log.info("Delete empty folders ...");

    // delete all empty folders
    final Archiver archiver = new Archiver(settings.getFolder());
    archiver.cleanEmptyFolders();

    log.info("... empty folders deleted successfully!");
  }
}
