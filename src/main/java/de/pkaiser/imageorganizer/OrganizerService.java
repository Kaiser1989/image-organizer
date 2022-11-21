package de.pkaiser.imageorganizer;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.pkaiser.imageorganizer.reader.AttributesReader;
import de.pkaiser.imageorganizer.reader.ChainedReader;
import de.pkaiser.imageorganizer.reader.DatedMediaFileReader;
import de.pkaiser.imageorganizer.reader.FilenamePattern;
import de.pkaiser.imageorganizer.reader.FilenameReader;
import de.pkaiser.imageorganizer.reader.MetadataReader;
import de.pkaiser.imageorganizer.writer.DatedMediaFileWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrganizerService {
		
	@Autowired
	private OrganizerSettings settings;
		
	public void organize() throws Exception {
		
		final DatedMediaFileReader reader = new ChainedReader(
			new MetadataReader(),
			new FilenameReader(
				// this
				new FilenamePattern("([0-9]{8}_[0-9]{6})(.*)\\.(jpg|mov|mp4)", 1, "yyyyMMdd_HHmmss"),
				// default
				new FilenamePattern("IMG_([0-9]{8}_[0-9]{6})(.*)\\.jpg", 1, "yyyyMMdd_HHmmss"),
				new FilenamePattern("VID_([0-9]{8}_[0-9]{6})(.*)\\.mp4", 1, "yyyyMMdd_HHmmss"),
				// pixel
				new FilenamePattern("PXL_([0-9]{8}_[0-9]{6})(.*)\\.jpg", 1, "yyyyMMdd_HHmmss"),
				// windows phone
				new FilenamePattern("WP_([0-9]{8}_[0-9]{2}_[0-9]{2}_[0-9]{2})(.*)\\.(jpg|mp4)", 1, "yyyyMMdd_HH_mm_ss"),
				// signal
				new FilenamePattern("signal-([0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2})(.*)\\.jpg", 1, "yyyy-MM-dd-HH-mm-ss"),
				// whatsapp
				new FilenamePattern("IMG-([0-9]{8})-WA(.*)\\.jpg", 1, "yyyyMMdd"),
				// other
				new FilenamePattern("([0-9]{8}_[0-9]{6})(.*)\\.mp4", 1, "yyyyMMdd_HHmmss")				
			),
			new AttributesReader()
		);
		
		final DatedMediaFileWriter writer = new DatedMediaFileWriter(settings.getPath());
		
		log.info("Searching for media files in {}", settings.getPath());
		
		for (File file : FileUtils.listFiles(
				new File(settings.getPath()), 
				new RegexFileFilter(mediaTypeRegex()), 
				DirectoryFileFilter.DIRECTORY)) {
			
			log.info("Processing file: {}", file.getPath());
			
			// read image
			final DatedMediaFile datedFile = reader.read(file);
			
			// write image
			writer.write(datedFile);
		}
		
		log.info("Cleanup empty directories");
		
		// clear empty folders
		this.deleteEmptyDirectories(new File(settings.getPath()));			
	}
	
	private String mediaTypeRegex() {
		final String endings = Stream.concat(
			DatedMediaFile.IMAGE_TYPES.stream(), 
			DatedMediaFile.VIDEO_TYPES.stream()
		).collect(Collectors.joining("|"));
		return String.format("^.*?\\.(%s|%s)$", endings.toLowerCase(), endings.toUpperCase());
	}
	
	private void deleteEmptyDirectories(final File parent) {
		for (final File child : parent.listFiles()) {
			if (child.isDirectory()) {			
				deleteEmptyDirectories(child);
				if (child.listFiles().length == 0) {
					child.delete();
				}
			}			
		}
	}
	
}
