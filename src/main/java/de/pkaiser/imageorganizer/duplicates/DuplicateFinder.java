package de.pkaiser.imageorganizer.duplicates;

import de.pkaiser.imageorganizer.Settings;
import de.pkaiser.imageorganizer.archive.MediaFileVisitor;
import de.pkaiser.imageorganizer.meta.MetaData;
import de.pkaiser.imageorganizer.meta.MetaData.Entity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class DuplicateFinder {

  private Settings settings;

  private MetaData metaData;

  public void findDuplicates() throws IOException {
    // mark metadata as initialized
    if (!this.metaData.isInitialized()) {
      log.warn("Skip! Metadata is not initialized yet!");
      return;
    }

    log.info("Calculating histograms for duplication matching ...");

    // sort by date
    final List<HistogramEntity> entities = this.metaData.getFiles().values().stream()
        .sorted(Comparator.comparing(x -> x.getDate().getEpochSecond()))
        .parallel().filter(entity -> {
          final String extension = FilenameUtils.getExtension(entity.getPath().toFile().getName())
              .toLowerCase();
          return MediaFileVisitor.IMAGE_TYPES.contains(extension);
        }).map(entity -> {
          try {
            log.info("Calculating histogram for file: {}", entity.getPath());
            final Histogram histogram = new Histogram(entity.getPath().toFile());
            return new HistogramEntity(entity, histogram);
          } catch (IOException e) {
            return null;
          }
        }).filter(Objects::nonNull).toList();

    log.info("... histograms calculated successfully!");

    log.info("Searching for duplicates ...");

    // final File dups
    final Set<File> duplicates = new HashSet<>();
    final Set<File> deletes = new HashSet<>();
    final Map<String, Integer> duplicateGroup = new HashMap<>();
    int group = 0;

    // find duplicates and move to subfolder duplicates
    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {

        final Histogram h1 = entities.get(i).getHistogram();
        final Histogram h2 = entities.get(j).getHistogram();
        Similarity similarity = Histogram.checkSimilarity(h1, h2);

        // early out
        if (similarity == Similarity.DIFFERENT) {
          continue;
        }

        final String p1 = h1.getFile().toPath().toString();
        final String p2 = h2.getFile().toPath().toString();

        // they are the same, delete one
        if (similarity == Similarity.SAME) {
          // they are the same, safe to delete
          log.info("Found duplicates (SAME): " + p1 + " == " + p2);
          deletes.add(h2.getFile());
          continue;
        }

        if (similarity == Similarity.SIMILAR) {
          // they are similar, user needs to decide
          log.info("Found duplicates (SIMILAR): " + p1 + " == " + p2);

          // add to duplicates
          duplicates.add(h1.getFile());
          duplicates.add(h2.getFile());

          // put into buckets
          if (duplicateGroup.containsKey(p1)) {
            duplicateGroup.put(p2, duplicateGroup.get(p1));
          } else if (duplicateGroup.containsKey(p2)) {
            duplicateGroup.put(p1, duplicateGroup.get(p2));
          } else {
            duplicateGroup.put(p1, group);
            duplicateGroup.put(p2, group);
            group++;
          }
        }
      }
    }

    log.info("... search finished successfully!");

    log.info("Moving duplicates ...");

    // move all duplicates to separate folder
    if (!deletes.isEmpty()) {

      // create dub folder
      final Path dupFolder = Path.of(settings.getFolder()).resolve("deletes");
      Files.createDirectories(dupFolder);

      // move all duplicate files
      for (final File file : deletes) {
        Files.move(file.toPath(), dupFolder.resolve(file.getName()));
      }
    }

    // move all duplicates to separate folder
    if (!duplicates.isEmpty()) {

      // create dub folder
      final Path dupFolder = Path.of(settings.getFolder()).resolve("duplicates");
      Files.createDirectories(dupFolder);

      // move all duplicate files
      for (final File file : duplicates) {
        if (deletes.contains(file)) {
          continue;
        }

        final int myGroup = duplicateGroup.get(file.toPath().toString());
        final Path groupFolder = dupFolder.resolve(String.valueOf(myGroup));
        Files.createDirectories(groupFolder);
        Files.move(file.toPath(), groupFolder.resolve(file.getName()));
      }
    }

    log.info("... duplicates moved successfully");
  }

  public void mergeDuplicates() throws IOException {
    log.info("Merging duplicates back ...");

    // move from duplicates back to root
    final Path root = Path.of(settings.getFolder());
    final Path dupFolder = root.resolve("duplicates");
    new MediaFileVisitor(dupFolder.toString()).run(path -> {
      try {
        Files.move(path, root.resolve(path.toFile().getName()));
      } catch (IOException e) {
        log.warn("Failed to move file : " + path);
      }
    });

    log.info("... duplicates merged successfully");
  }

  @Data
  @AllArgsConstructor
  public static class HistogramEntity {

    private Entity entity;

    private Histogram histogram;
  }
}
