package de.pkaiser.imageorganizer;

import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import de.pkaiser.imageorganizer.meta.MetaDataReader;
import de.pkaiser.imageorganizer.meta.MetaDataRestorer;

public class MetaDataTest {

	private static final Path TEST_IMAGE_1 = FileUtils.getFile("src", "test", "resources", "test_image1.jpg").toPath();
	private static final Path TEST_IMAGE_2 = FileUtils.getFile("src", "test", "resources", "test_image2.jpg").toPath();

	@Test
	public void readFile1() throws Exception {
		new MetaDataRestorer().restore(TEST_IMAGE_1);
	}

	@Test
	public void readFile2() throws Exception {
		new MetaDataRestorer().restore(TEST_IMAGE_2);
		System.out.println(new MetaDataReader().read(TEST_IMAGE_2).get());
	}
}
