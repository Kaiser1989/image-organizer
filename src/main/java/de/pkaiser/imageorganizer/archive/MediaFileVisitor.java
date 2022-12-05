package de.pkaiser.imageorganizer.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MediaFileVisitor {

	public static Set<String> IMAGE_TYPES = new HashSet<>(Arrays.asList("jpg", "png", "gif"));
	public static Set<String> VIDEO_TYPES = new HashSet<>(Arrays.asList("avi", "mp4", "mov"));

	private String path;

	public void run(final Consumer<Path> processor) throws IOException {
		final String[] extensions = Stream.concat(IMAGE_TYPES.stream(), IMAGE_TYPES.stream()).toArray(String[]::new);

		try (Stream<Path> stream = Files.walk(Paths.get(path))) {
			stream.filter(Files::isRegularFile).forEach(filePath -> {
				log.info("Processing file: {}", filePath);
				processor.accept(filePath);
			});
		}

		FileUtils.streamFiles(new File(path), true, extensions).forEach(file -> {
			log.info("Processing file: {}", file.getPath());
			processor.accept(file.toPath());
		});
	}
}
