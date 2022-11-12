package de.pkaiser.imageorganizer.reader;

import java.io.File;

import de.pkaiser.imageorganizer.DatedMediaFile;

public interface DatedMediaFileReader {

	public DatedMediaFile read(final File file) throws Exception;
}
