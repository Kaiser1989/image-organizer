package de.pkaiser.imageorganizer.duplicates;

import de.pkaiser.imageorganizer.archive.MediaFileVisitor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

@Slf4j
@AllArgsConstructor
public class DuplicateFinder {

  private String path;

  public void clean() throws IOException {
    final File root = Paths.get(this.path).toFile();
    for (final File year : Objects.requireNonNull(root.listFiles(File::isDirectory))) {
      for (final File month : Objects.requireNonNull(year.listFiles(File::isDirectory))) {

        // final File dups
        final Set<File> duplicates = new HashSet<>();

        // calculate histograms for all files of a folder
        final List<Histogram> histograms = new ArrayList<>();
        for (final File file : Objects.requireNonNull(month.listFiles(File::isFile))) {
          final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
          if (MediaFileVisitor.IMAGE_TYPES.contains(extension)) {
            log.info("Calculating histogram for file: {}", file.toPath());
            histograms.add(new Histogram(file));
          }
        }

        // find duplicates and move to subfolder duplicates
        for (int i = 0; i < histograms.size(); i++) {
          for (int j = i + 1; j <  histograms.size(); j++) {

            final Histogram h1 = histograms.get(i);
            final Histogram h2 = histograms.get(j);

            if (Histogram.isSimilar(h1, h2)) {
              // they are similar, so move the smaller dup to duplicates folder
              final File dup = findLowQuality(h1.getFile(), h2.getFile());
              System.out.println("Found duplicate: " + dup.toPath());
              duplicates.add(dup);
            }
          }
        }

        // move all duplicates to separate folder
        System.out.println("Moving duplicates ...");
        if (!duplicates.isEmpty()) {

          // create dub folder
          final Path dupFolder = month.toPath().resolve("duplicates");
          Files.createDirectories(dupFolder);

          // move all duplicate files
          for (final File file : duplicates) {
            Files.move(file.toPath(), dupFolder.resolve(file.getName()));
          }
        }
      }
    }
  }

  public static File findLowQuality(final File f1, final File f2) {
    return f1.length() < f2.length() ? f1 : f2;
  }

}
