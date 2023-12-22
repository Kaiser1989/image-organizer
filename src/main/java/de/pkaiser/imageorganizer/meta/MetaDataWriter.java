package de.pkaiser.imageorganizer.meta;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

@Slf4j
public class MetaDataWriter {

  private static final String PATTERN_FORMAT = "yyyy:MM:dd hh:mm:ss";

  private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern(
      PATTERN_FORMAT).withZone(ZoneId.systemDefault());

  public void update(final Path path, final Instant time) {
    Path temp = null;
    try {
      // update metadata (delete old entry before adding new one
      final TiffOutputSet outputSet = this.extractMetaData(path);
      final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
      exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
      exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
          INSTANT_FORMATTER.format(time));

      // create temp file, with output stream
      temp = Files.createTempFile("new_" + path.getFileName(), null);
      try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(temp.toFile()))) {

        // update exif (use temp file and swap)
        new ExifRewriter().updateExifMetadataLossless(path.toFile(), os, outputSet);
        swapFiles(path, temp);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ImageWriteException | ImageReadException e) {
      log.warn("Failed to write metadata: {}", e.getMessage());
    } finally {
      if (temp != null) {
        try {
          Files.delete(temp);
        } catch (IOException e) {
          log.warn("Failed to delete temp file: {}", path);
        }
      }
    }
  }

  private boolean isImage(final Path path) {
    return Imaging.hasImageFileExtension(path.toFile());
  }

  private TiffOutputSet extractMetaData(final Path path)
      throws ImageReadException, IOException, ImageWriteException {
    // note that metadata might be null if no metadata is found.
    final ImageMetadata metadata = Imaging.getMetadata(path.toFile());
    if (metadata instanceof JpegImageMetadata jpegMetadata) {
      // note that exif might be null if no Exif metadata is found.
      final TiffImageMetadata exif = jpegMetadata.getExif();
      if (null != exif) {
        return exif.getOutputSet();
      }
    }

    // return new empty meta data if non exists
    return new TiffOutputSet();
  }

  private void swapFiles(final Path file1, final Path file2) {
    final Path tmp = file1.resolveSibling("~" + file1.getFileName().toString());
    try {
      Files.move(file1, tmp);
      Files.move(file2, file1);
      Files.move(tmp, file2);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
