package de.pkaiser.imageorganizer;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DatedMediaFile {

	public static Set<String> IMAGE_TYPES = new HashSet<>(Arrays.asList("jpg", "png", "gif"));
	public static Set<String> VIDEO_TYPES = new HashSet<>(Arrays.asList("avi", "mp4", "mov"));

	private File file;
	
	private Instant date;

	private MediaFileType mediaType;
	
	public DatedMediaFile(final File file, final Instant date) {
		this.file = file;
		this.date = date;
		
		final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();	
		if (IMAGE_TYPES.contains(extension)) {
			this.mediaType = MediaFileType.IMAGE;
		} else {
			this.mediaType = MediaFileType.VIDEO;
		}
	}
	
	public static enum MediaFileType {
		IMAGE,
		VIDEO;
	}
}
