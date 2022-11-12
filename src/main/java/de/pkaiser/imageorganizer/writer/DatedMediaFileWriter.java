package de.pkaiser.imageorganizer.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.io.FilenameUtils;

import de.pkaiser.imageorganizer.DatedMediaFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatedMediaFileWriter {

	private final String targetDirectory;
	
	public DatedMediaFileWriter(final String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	
	public void write(final DatedMediaFile image) throws IOException {
		final PathBuilder builder = this.createPathBuilder(image);

		// check if path changed
		Path path = builder.build();
		if (image.getFile().toPath().equals(path)) {
			log.debug("No change for file: {}", path);
		}
		
		// update until we find unique id
		while (path.toFile().exists()) {
			log.info("File with name {} already exists", path);
			builder.updateId();
			path = builder.build();
		}
		
		// try to move/copy data
//		Files.createDirectories(path.getParent());
//		Files.copy(image.getFile().toPath(), path, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
//		log.info("Write file to {}", path.toString());		
	}
	 
	public PathBuilder createPathBuilder(final DatedMediaFile image) {
		final ZonedDateTime date = image.getDate().atZone(ZoneOffset.UTC);
		final String datePart = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		final String idPart = String.format("%02d%02d%02d00", date.getHour(), date.getMinute(), date.getSecond());
		final Path directory = new File(this.targetDirectory).toPath().resolve(String.format("%d/%d", date.getYear(), date.getMonthValue()));
		final String extension = FilenameUtils.getExtension(image.getFile().getName());
		final String prefix = switch (image.getMediaType()) {
			case IMAGE -> "IMG";
			case VIDEO -> "VID";
		};
		return new PathBuilder(directory, prefix, datePart, idPart, extension);
	}
	
	protected static class PathBuilder {
						
		private Path directory;
		
		private String prefix;
		
		private String datePart;
		
		private String idPart;
		
		private String extension;
		
		public PathBuilder(final Path directory, final String prefix, final String datePart, final String idPart, final String extension) {
			this.directory = directory;
			this.prefix = prefix;
			this.datePart = datePart;
			this.idPart = idPart;
			this.extension = extension;
		}
		
		public Path build() {
			return this.directory.resolve(String.format("%s_%s_%s.%s", this.prefix, this.datePart, this.idPart, this.extension));
		}
		
		public void updateId() {
			this.idPart = String.valueOf(Integer.valueOf(this.idPart) + 1);
		}	
	}	
}
