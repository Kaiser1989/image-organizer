package de.pkaiser.imageorganizer.archive;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

public class ImageComparator {

	public static boolean equals(final Path path1, final Path path2) {
		try {
			if (Files.size(path1) == Files.size(path2)) {

				// check if both paths are images
				final String extension = FilenameUtils.getExtension(path1.getFileName().toString()).toLowerCase();
				if (!MediaFileVisitor.IMAGE_TYPES.contains(extension)) {
					return true;
				}

				// process images
				BufferedImage img1 = ImageIO.read(path1.toFile());
				BufferedImage img2 = ImageIO.read(path2.toFile());
				if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {

					// check random values
					final int[] xs = new Random().ints(3, 0, img1.getWidth()).sorted().toArray();
					final int[] ys = new Random().ints(3, 0, img1.getHeight()).sorted().toArray();
					for (int x = 0; x < xs.length; x++) {
						for (int y = 0; y < ys.length; y++) {
							if (img1.getRGB(x, y) != img2.getRGB(x, y))
								return false;
						}
					}
				} else {
					return false;
				}
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
}
