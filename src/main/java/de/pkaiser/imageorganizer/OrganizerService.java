package de.pkaiser.imageorganizer;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.pkaiser.imageorganizer.archive.Archiver;
import de.pkaiser.imageorganizer.archive.MediaFileVisitor;
import de.pkaiser.imageorganizer.meta.MetaDataRestorer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrganizerService {

	@Autowired
	private Settings settings;

	public void organize() throws Exception {

		// save files to save file tree iterations
		Map<Path, Optional<Instant>> files = new LinkedHashMap<>();
		List<Path> failedFiles = new ArrayList<>();

		// first of all, restore all meta data
		log.info("Start restoring metadata ...");
		final MetaDataRestorer restorer = new MetaDataRestorer();
		new MediaFileVisitor(settings.getPath()).run(path -> {
			try {
				files.put(path, restorer.restore(path));
			} catch (IllegalArgumentException e) {
				failedFiles.add(path);
			}
		});

		log.info("Start moving files ...");
		final Archiver archiver = new Archiver(settings.getPath());
		files.forEach((path, optTime) -> {
			optTime.ifPresent(time -> archiver.archive(path, time));
		});

		failedFiles.forEach(path -> log.info("File cannot be processed {}", path));

		log.info("delete empty folders ...");
		archiver.cleanEmptyFolders();
	}
}
