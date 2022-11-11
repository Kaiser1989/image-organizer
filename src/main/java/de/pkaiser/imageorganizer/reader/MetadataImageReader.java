package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import de.pkaiser.imageorganizer.Image;

public class MetadataImageReader implements ImageReader {

	@Override
	public Image read(final File file) throws FileNotFoundException, IOException, ImageProcessingException  {
		try (FileInputStream stream = new FileInputStream(file)) {
			final Metadata metadata = ImageMetadataReader.readMetadata(stream);
			ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (directory != null) {
				final Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				if (date != null) {
					return new Image(file, date.toInstant());
				}
			}
			return null;
		}
	}

}
