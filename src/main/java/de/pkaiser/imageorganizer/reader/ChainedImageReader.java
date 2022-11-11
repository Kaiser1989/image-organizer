package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.pkaiser.imageorganizer.Image;

public class ChainedImageReader implements ImageReader {

	private List<ImageReader> readers;
	
	public ChainedImageReader(final ImageReader... readers) {
		this.readers = Arrays.asList(readers);
	}
	
	@Override
	public Image read(final File file) throws Exception {
		Image img = null;
		for (final ImageReader reader : readers) {
			img = img == null ? reader.read(file) : img;
		}
		return img;
	}
}
