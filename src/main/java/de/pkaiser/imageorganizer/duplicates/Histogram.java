package de.pkaiser.imageorganizer.duplicates;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.Getter;

public class Histogram {

  @Getter
  private final File file;

  private final int width;

  private final int height;

  private final float[][][] channels;

  public Histogram(final File file) throws IOException {
    this.file = file;
    this.channels = new float[4][4][4];

    final BufferedImage image = ImageIO.read(file);
    this.width = image.getWidth();
    this.height = image.getHeight();

    final int res = this.width * this.height;
    final Raster raster = image.getData();
    final int bands = raster.getNumBands();
    if (bands > 4) {
      throw new IllegalArgumentException("Does not support images with higher bands");
    }
    final byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();

    for (int x = 0; x < res * bands; x += bands) {
      int[] ch = new int[4];
      for (int i = 0; i < bands; i++) {
        ch[i] = data[x + i] & 0xff;
      }
      channels[ch[0] / 64][ch[1] / 64][ch[2] / 64]++;
    }
    for (int i = 0; i < channels.length; i++) {
      for (int j = 0; j < channels[i].length; j++) {
        for (int p = 0; p < channels[i][j].length; p++) {
          channels[i][j][p] = (channels[i][j][p] / res) * 100;
        }
      }
    }
  }

  public static Similarity checkSimilarity(final Histogram h1, final Histogram h2) {
    // they are not similar, they are the same
    if (h1.file.getName().equals(h2.file.getName())) {
      return Similarity.SAME;
    }

    // compare histograms
    float sum = 0;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        for (int p = 0; p < 4; p++) {
          sum += Math.abs(h1.channels[i][j][p] - h2.channels[i][j][p]);
        }
      }
    }

    // evaluate result
    if (sum < 0.001 && matchFileSize(h1, h2)) {
      return Similarity.SAME;
    } else if (sum <= 2) {
      return Similarity.SIMILAR;
    } else {
      return Similarity.DIFFERENT;
    }
  }

  private static boolean matchFileSize(final Histogram h1, final Histogram h2) {
    return h1.width == h2.width && h1.height == h2.height && h1.getFile().length() == h2.getFile()
        .length();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Histogram histogram = (Histogram) o;
    return Objects.equals(this.file.getName(), histogram.file.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.file.getName());
  }
}
