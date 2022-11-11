package de.pkaiser.imageorganizer;

import java.io.File;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Image {

	private File file;
	
	private Instant date;
}
