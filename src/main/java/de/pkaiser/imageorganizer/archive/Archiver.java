package de.pkaiser.imageorganizer.archive;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Archiver {

	private String path;

	public void archive(final Path path, final Instant time) {
		final PathBuilder builder = this.createPathBuilder(path, time);

		// check if path changed
		Path newPath = builder.build();
		if (newPath.equals(path)) {
			log.debug("No change for file: {}", path);
			return;
		}

		// update until we find unique id
		while (newPath.toFile().exists()) {
			log.info("File with name {} already exists", newPath);
			builder.updateId();
			newPath = builder.build();
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
		final ZonedDateTime date = time.atZone(ZoneId.systemDefault());
		final String datePart = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(),
				date.getDayOfMonth());
		final String idPart = String.format("%02d%02d%02d00", date.getHour(), date.getMinute(), date.getSecond());
		final Path directory = new File(this.path).toPath()
				.resolve(String.format("%d/%d", date.getYear(), date.getMonthValue()));
		final String extension = FilenameUtils.getExtension(path.getFileName().toString());
		return new PathBuilder(directory, datePart, idPart, extension);
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

		private String idPart;

		private String extension;

		public PathBuilder(final Path directory, final String datePart, final String idPart, final String extension) {
			this.directory = directory;
			this.datePart = datePart;
			this.idPart = idPart;
			this.extension = extension;
		}

		public Path build() {
			return this.directory
					.resolve(String.format("%s_%s.%s", this.datePart, this.idPart, this.extension.toLowerCase()));
		}

		public void updateId() {
			this.idPart = String.valueOf(Integer.valueOf(this.idPart) + 1);
		}
	}
}
