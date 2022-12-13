package de.pkaiser.imageorganizer;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
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

		// first of all, restore all meta data
		log.info("Start restoring metadata ...");
		final MetaDataRestorer restorer = new MetaDataRestorer();
		new MediaFileVisitor(settings.getFolder()).run(path -> {
			files.put(path, restorer.restore(path));
		});

		// check if we should stop after restoring
		if (!settings.isArchive()) {
			return;
		}

		log.info("Start moving files ...");
		final Archiver archiver = new Archiver(settings.getFolder());
		files.forEach((path, optTime) -> {
			optTime.ifPresent(time -> archiver.archive(path, time));
		});

		log.info("delete empty folders ...");
		archiver.cleanEmptyFolders();
	}
}
