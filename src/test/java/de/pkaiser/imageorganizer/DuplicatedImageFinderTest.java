package de.pkaiser.imageorganizer;


import de.pkaiser.imageorganizer.duplicates.Histogram;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class DuplicatedImageFinderTest {

  private static final Path TEST_IMAGE_1 = FileUtils.getFile("src", "test", "resources",
      "test_same1.jpg").toPath();
  private static final Path TEST_IMAGE_2 = FileUtils.getFile("src", "test", "resources",
      "test_same2.jpg").toPath();

  private static final Path TEST_IMAGE_3 = FileUtils.getFile("src", "test", "resources",
      "test_same3.jpg").toPath();

  private static final Path TEST_IMAGE_4 = FileUtils.getFile("src", "test", "resources",
      "test_same4.jpg").toPath();

  private static final Path TEST_IMAGE_5 = FileUtils.getFile("src", "test", "resources",
      "test_not_same1.jpg").toPath();

  private static final Path TEST_IMAGE_6 = FileUtils.getFile("src", "test", "resources",
      "test_not_same2.jpg").toPath();

  private static final Path TEST_IMAGE_7 = FileUtils.getFile("src", "test", "resources",
      "black.jpg").toPath();

  @Test
  public void test() throws Exception {
    long tick = System.currentTimeMillis();
    final Histogram h1 = new Histogram(TEST_IMAGE_1.toFile());
    final Histogram h2 = new Histogram(TEST_IMAGE_2.toFile());
    final Histogram h3 = new Histogram(TEST_IMAGE_3.toFile());
    final Histogram h4 = new Histogram(TEST_IMAGE_4.toFile());

    final Histogram h5 = new Histogram(TEST_IMAGE_5.toFile());
    final Histogram h6 = new Histogram(TEST_IMAGE_6.toFile());
    System.out.println("Tack: " + (System.currentTimeMillis() - tick));

    System.out.println("Same image: " + Histogram.checkSimilarity(h1, h2));
    System.out.println("Same image: " + Histogram.checkSimilarity(h1, h3));
    System.out.println("Same image: " + Histogram.checkSimilarity(h2, h4));
    System.out.println("Same image: " + Histogram.checkSimilarity(h5, h6));
  }

  @Test
  public void testBlackChannel() throws Exception {
    final Histogram h = new Histogram(TEST_IMAGE_6.toFile());
  }
}
