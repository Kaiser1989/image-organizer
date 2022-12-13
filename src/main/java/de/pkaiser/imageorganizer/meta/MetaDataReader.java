package de.pkaiser.imageorganizer.meta;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetaDataReader {

	private static FilenamePattern[] IMAGE_PATTERNS = {
			// default
			new FilenamePattern("[a-zA-Z]+[-_]([0-9]{8}_[0-9]{6})(.*)\\.*", 1, "yyyyMMdd_HHmmss"),
			// windows phone
			new FilenamePattern("[a-zA-Z]+[-_]([0-9]{8}_[0-9]{2}_[0-9]{2}_[0-9]{2})(.*)\\.*", 1, "yyyyMMdd_HH_mm_ss"),
			// signal
			new FilenamePattern("[a-zA-Z]+[-_]([0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2})(.*)\\.*", 1,
					"yyyy-MM-dd-HH-mm-ss"),
			// whatsapp
			new FilenamePattern("[a-zA-Z]+[-_]([0-9]{8})(.*)\\.*", 1, "yyyyMMdd") };

	public boolean hasCreationDate(final Path path) {
		return this.read(path).isPresent();
	}

	public Optional<Instant> read(final Path path) {
		try (FileInputStream stream = new FileInputStream(path.toFile())) {
			final Metadata metadata = ImageMetadataReader.readMetadata(stream);
			final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (directory != null) {
				final Date date = directory.getDateOriginal();
				if (date != null) {
					return Optional.of(date.toInstant());
				}
			}
		} catch (IOException e1) {
			throw new UncheckedIOException(e1);
		} catch (ImageProcessingException e) {
			return Optional.empty();
		}
		return Optional.empty();
	}

	public Optional<Instant> readFromFilename(final Path path) {

		for (final FilenamePattern pattern : IMAGE_PATTERNS) {
			final Optional<Instant> time = pattern.tryParse(path.getFileName().toString());
			if (time.isPresent()) {
				return time;
			}
		}

		log.warn("Unknown pattern: {}", path);

		return Optional.empty();
	}

	public Optional<Instant> readFromAttributes(final Path path) {
		try {
			final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			final Instant timeCreated = attr.creationTime().toInstant();
			final Instant timeModified = attr.lastModifiedTime().toInstant();
			return Optional.of(timeCreated.isBefore(timeModified) ? timeCreated : timeModified);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static class FilenamePattern {

		private final Pattern pattern;

		private final int dateGroup;

		private final DateTimeFormatter dateFormatter;

		public FilenamePattern(final String pattern, final int dateGroup, final String dateFormat) {
			this.pattern = Pattern.compile(pattern);
			this.dateGroup = dateGroup;
			this.dateFormatter = new DateTimeFormatterBuilder().appendPattern(dateFormat)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
					.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
					.toFormatter().withZone(ZoneId.of("UTC"));
		}

		public Optional<Instant> tryParse(final String filename) {
			final Matcher matcher = this.pattern.matcher(filename);
			if (matcher.matches()) {
				final String dateString = matcher.group(dateGroup);
				try {
					return Optional.of(dateFormatter.parse(dateString, Instant::from));
				} catch (DateTimeParseException e) {
					log.error(String.format("Failed to parse date string: %s", dateString), e);
				}
			}
			return Optional.empty();
		}
	}
}
