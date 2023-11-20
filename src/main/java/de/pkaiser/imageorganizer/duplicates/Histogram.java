package de.pkaiser.imageorganizer.duplicates;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.Getter;

public class Histogram {

  @Getter
  private final File file;

  private final float[][][] channels;

  public Histogram(final File file) throws IOException {
    this.file = file;
    this.channels = new float[2][2][2];
    BufferedImage image = ImageIO.read(file);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int color = image.getRGB(x, y);
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;
        channels[red / 128][green / 128][blue / 128]++;
      }
    }
    int res = image.getWidth() * image.getHeight();
    for (int i = 0; i < channels.length; i++) {
      for (int j = 0; j < channels[i].length; j++) {
        for (int p = 0; p < channels[i][j].length; p++) {
          channels[i][j][p] = (channels[i][j][p] / res) * 100;
        }
      }
    }
  }

  public static boolean isSimilar(final Histogram h1, final Histogram h2) {
    // they are not similar, they are the same
    if (h1.file.getName().equals(h2.file.getName())) {
      return false;
    }

    // compare histograms
    float[][][] ch = new float[2][2][2];
    for (int i = 0; i < ch.length; i++) {
      for (int j = 0; j < ch[i].length; j++) {
        for (int p = 0; p < ch[i][j].length; p++) {
          ch[i][j][p] = Math.abs(h1.channels[i][j][p] - h2.channels[i][j][p]);
          //System.out.println("t[" + i + "][" + j + "][" + p + "] = " + ch[i][j][p]);
          if (ch[i][j][p] > 1) {
            return false;
          }
        }
      }
    }
    return true;
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
