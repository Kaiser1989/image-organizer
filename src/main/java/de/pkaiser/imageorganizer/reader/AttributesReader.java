package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

import de.pkaiser.imageorganizer.DatedMediaFile;

public class AttributesReader implements DatedMediaFileReader {

	@Override
	public DatedMediaFile read(File file) throws Exception {
		final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		final Instant timeCreated = attr.creationTime().toInstant();
		final Instant timeModified = attr.lastModifiedTime().toInstant();
		final Instant time = timeCreated.isBefore(timeModified) ? timeCreated : timeModified;
		return new DatedMediaFile(file, time);
	}

}
