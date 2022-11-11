package de.pkaiser.imageorganizer.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import de.pkaiser.imageorganizer.Image;

public class ImageWriter {

	private final String targetDirectory;
	
	public ImageWriter(final String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	
	public void write(final Image image) throws IOException {
		final ImagePathBuilder builder = this.createPathBuilder(image);
		Path path = null;
		while ((path = builder.build()).toFile().exists()) {
			builder.updateId();
		}
		
		// try to move/copy data
		Files.createDirectories(path.getParent());
		Files.copy(image.getFile().toPath(), path, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		//System.out.println(image.getFile().toPath().toString() + " -> " + path.toString());
	}
	 
	public ImagePathBuilder createPathBuilder(final Image image) {
		final ZonedDateTime date = image.getDate().atZone(ZoneOffset.UTC);
		final String datePart = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		final String idPart = String.format("%02d%02d%02d00", date.getHour(), date.getMinute(), date.getSecond());
		final Path directory = new File(this.targetDirectory).toPath().resolve(String.format("%d/%d", date.getYear(), date.getMonthValue()));
		return new ImagePathBuilder(directory, datePart, idPart);
	}
	
	protected static class ImagePathBuilder {
		
		private static final String PREFIX = "IMG";
		
		private Path directory;
		
		private String datePart;
		
		private String idPart;
		
		public ImagePathBuilder(final Path directory, final String datePart, final String idPart) {
			this.directory = directory;
			this.datePart = datePart;
			this.idPart = idPart;
		}
		
		public Path build() {
			return this.directory.resolve(String.format("%s_%s_%s.jpg", PREFIX, this.datePart, this.idPart));
		}
		
		public void updateId() {
			this.idPart = String.valueOf(Integer.valueOf(this.idPart) + 1);
		}	
	}	
}
