package de.pkaiser.imageorganizer.archive;

import static java.util.regex.Pattern.quote;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Archiver {

	private String path;

	public void archive(final Path path, final Instant time) {
		final PathBuilder builder = this.createPathBuilder(path, time);

		// check if folder should be adjusted
		final boolean matchFolder = builder.matchFolder(path.getParent());
		final boolean matchFilename = builder.matchFilename(path.getFileName());

		// check if path changed
		if (matchFolder && matchFilename) {
			log.debug("No change for file: {}", path);
			return;
		}

		// update until we find unique id
		Path newPath;
		while ((newPath = builder.build()).toFile().exists()) {
			if (ImageComparator.equals(path, newPath)) {
				log.info("Deleting duplicated file: {}", path);
				try {
					Files.delete(path);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				return;
			}
			log.info("File with name {} already exists", newPath);
			builder.updateId();
		}

		try {
			// try to move data
			Files.createDirectories(newPath.getParent());
			Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
			log.info("Moved file: {} -> {}", path, newPath);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void cleanEmptyFolders() {
		this.cleanEmptyFolders(new File(this.path));
	}

	private PathBuilder createPathBuilder(final Path path, final Instant time) {
		final ZonedDateTime date = time.atZone(ZoneId.of("UTC"));
		final String datePart = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(),
				date.getDayOfMonth());
		final String timePart = String.format("%02d%02d%02d", date.getHour(), date.getMinute(), date.getSecond());
		final Path directory = new File(this.path).toPath()
				.resolve(String.format("%d/%02d", date.getYear(), date.getMonthValue()));
		final String extension = FilenameUtils.getExtension(path.getFileName().toString()).toLowerCase();
		return new PathBuilder(directory, datePart, timePart, extension);
	}

	private void cleanEmptyFolders(final File parent) {
		for (final File child : parent.listFiles()) {
			if (child.isDirectory()) {
				this.cleanEmptyFolders(child);
				if (child.listFiles().length == 0) {
					child.delete();
				}
			}
		}
	}

	protected static class PathBuilder {

		private Path directory;

		private String datePart;

		private String timePart;

		private String extension;

		private int idPart;

		public PathBuilder(final Path directory, final String datePart, final String timePart, final String extension) {
			this.directory = directory;
			this.datePart = datePart;
			this.timePart = timePart;
			this.extension = extension;
			this.idPart = 0;
		}

		public Path build() {
			return this.directory.resolve(
					String.format("%s%s_%s%s.%s", getPrefix(), this.datePart, this.timePart, getId(), this.extension));
		}

		public boolean matchFolder(final Path folder) {
			return this.directory.equals(folder);
		}

		public boolean matchFilename(final Path filename) {
			return Pattern.compile(String.format("%s%s_%s(_[0-9]{3})?.%s", quote(getPrefix()), quote(this.datePart),
					quote(this.timePart), quote(this.extension))).matcher(filename.toString()).matches();
		}

		public void updateId() {
			this.idPart++;
		}

		private String getPrefix() {
			if (MediaFileVisitor.IMAGE_TYPES.contains(this.extension)) {
				return "IMG_";
			} else if (MediaFileVisitor.VIDEO_TYPES.contains(this.extension)) {
				return "VID_";
			} else {
				return "";
			}
		}

		private String getId() {
			if (this.idPart > 0) {
				return String.format("_%03d", idPart);
			} else {
				return "";
			}
		}
	}
}
