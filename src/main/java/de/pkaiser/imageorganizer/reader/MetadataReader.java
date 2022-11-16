package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import de.pkaiser.imageorganizer.DatedMediaFile;

public class MetadataReader implements DatedMediaFileReader {

	@Override
	public DatedMediaFile read(final File file) throws FileNotFoundException, IOException, ImageProcessingException  {
		try (FileInputStream stream = new FileInputStream(file)) {
			final Metadata metadata = ImageMetadataReader.readMetadata(stream);
			ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (directory != null) {
				final Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
				if (date != null) {
					return new DatedMediaFile(file, date.toInstant());
				}
			}
		} catch (ImageProcessingException e) {}
		
		// log.warn("Missing metadata: {}", file.toPath());
		
		return null;
	}

}
