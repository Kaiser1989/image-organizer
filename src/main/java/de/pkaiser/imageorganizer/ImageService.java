package de.pkaiser.imageorganizer;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.pkaiser.imageorganizer.reader.AttributesImageReader;
import de.pkaiser.imageorganizer.reader.ChainedImageReader;
import de.pkaiser.imageorganizer.reader.FilenameImagePattern;
import de.pkaiser.imageorganizer.reader.FilenameImageReader;
import de.pkaiser.imageorganizer.reader.ImageReader;
import de.pkaiser.imageorganizer.reader.MetadataImageReader;
import de.pkaiser.imageorganizer.writer.ImageWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageService {
	
	private static final String IMAGE_REGEX = "^.*?\\.(jpg|JPG)$";
	
	@Autowired
	private ImageSettings settings;
		
	public void organize() throws Exception {
		
		final ImageReader reader = new ChainedImageReader(
			new MetadataImageReader(),
			new FilenameImageReader(
				new FilenameImagePattern("PXL_([0-9]{8}_[0-9]{6})(.*)\\.jpg", 1, "yyyyMMdd_HHmmss"),
				new FilenameImagePattern("IMG_([0-9]{8}_[0-9]{6})(.*)\\.jpg", 1, "yyyyMMdd_HHmmss"),
				new FilenameImagePattern("signal-([0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2})(.*)\\.jpg", 1, "yyyy-MM-dd-HH-mm-ss"),
				new FilenameImagePattern("IMG-([0-9]{8})-WA(.*)\\.jpg", 1, "yyyyMMdd")
			),
			new AttributesImageReader()
		);
		
		final ImageWriter writer = new ImageWriter(settings.getTarget());
		
		log.info("Searching for images in {}", settings.getOrigin());
		
		for (File file : FileUtils.listFiles(new File(settings.getOrigin()), new RegexFileFilter(IMAGE_REGEX), DirectoryFileFilter.DIRECTORY)) {
			
			// read image
			final Image img = reader.read(file);
			
			// write image
			writer.write(img);
		}
	}
}
