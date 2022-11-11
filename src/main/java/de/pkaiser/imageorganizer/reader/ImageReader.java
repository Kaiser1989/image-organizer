package de.pkaiser.imageorganizer.reader;

import java.io.File;

import de.pkaiser.imageorganizer.Image;

public interface ImageReader {

	public Image read(final File file) throws Exception;
}
