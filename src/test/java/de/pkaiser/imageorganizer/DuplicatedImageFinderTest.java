package de.pkaiser.imageorganizer;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class DuplicatedImageFinderTest {

  private static final Path TEST_IMAGE_1 = FileUtils.getFile("src", "test", "resources",
      "test_same1.jpg").toPath();
  private static final Path TEST_IMAGE_2 = FileUtils.getFile("src", "test", "resources",
      "test_same2.jpg").toPath();

  private static final Path TEST_IMAGE_3 = FileUtils.getFile("src", "test", "resources",
      "test_same3.jpg").toPath();

  @Test
  public void test() throws Exception {
    float[][][] h1 = calculateHistogram(TEST_IMAGE_1.toFile());
    float[][][] h2 = calculateHistogram(TEST_IMAGE_2.toFile());
    float[][][] h3 = calculateHistogram(TEST_IMAGE_3.toFile());

    System.out.println("Same image: " + sameImage(h1, h2));
    System.out.println("Same image: " + sameImage(h1, h3));
  }

  public float[][][] calculateHistogram(final File file) throws IOException {
    float[][][] ch = new float[2][2][2];
    BufferedImage image = ImageIO.read(file);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int color = image.getRGB(x, y);
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;
        ch[red / 128][green / 128][blue / 128]++;
      }
    }
    int res = image.getWidth() * image.getHeight();
    for (int i = 0; i < ch.length; i++) {
      for (int j = 0; j < ch[i].length; j++) {
        for (int p = 0; p < ch[i][j].length; p++) {
          ch[i][j][p] = (ch[i][j][p] / res) * 100;
        }
      }
    }
    return ch;
  }

  public boolean sameImage(float[][][] h1, float[][][] h2) {
    float[][][] ch = new float[2][2][2];
    for (int i = 0; i < ch.length; i++) {
      for (int j = 0; j < ch[i].length; j++) {
        for (int p = 0; p < ch[i][j].length; p++) {
          ch[i][j][p] = Math.abs(h1[i][j][p] - h2[i][j][p]);
          //System.out.println("t[" + i + "][" + j + "][" + p + "] = " + ch[i][j][p]);
          if (ch[i][j][p] > 1) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
