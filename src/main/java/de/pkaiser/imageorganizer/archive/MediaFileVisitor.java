package de.pkaiser.imageorganizer.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MediaFileVisitor {

	public static Set<String> IMAGE_TYPES = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif"));
	public static Set<String> VIDEO_TYPES = new HashSet<>(Arrays.asList("avi", "mp4", "mov"));

	private String path;

	public void run(final Consumer<Path> processor) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(path))) {
			stream.filter(Files::isRegularFile).filter(filePath -> {
				final String extension = FilenameUtils.getExtension(filePath.getFileName().toString()).toLowerCase();
				return IMAGE_TYPES.contains(extension) || VIDEO_TYPES.contains(extension);
			}).forEach(filePath -> {
				log.info("Processing file: {}", filePath);
				processor.accept(filePath);
			});
		}
	}
}
