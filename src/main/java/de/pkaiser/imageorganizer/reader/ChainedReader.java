package de.pkaiser.imageorganizer.reader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.pkaiser.imageorganizer.DatedMediaFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChainedReader implements DatedMediaFileReader {

	private List<DatedMediaFileReader> readers;
	
	public ChainedReader(final DatedMediaFileReader... readers) {
		this.readers = Arrays.asList(readers);
	}
	
	@Override
	public DatedMediaFile read(final File file) throws Exception {
		DatedMediaFile datedFile = null;
		for (final DatedMediaFileReader reader : readers) {
			datedFile = datedFile == null ? reader.read(file) : datedFile;
		}
		
		if (datedFile == null) {
			log.warn("No matching chain: {}", file.toString());
		}
		
		return datedFile;
	}
}
