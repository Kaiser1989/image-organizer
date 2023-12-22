package de.pkaiser.imageorganizer;

import de.pkaiser.imageorganizer.duplicates.DuplicateFinder;
import java.io.File;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class Application {

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

    final Scanner scanner = new Scanner(System.in);

    log.info("Welcome to the image organizer!");
    log.info("");
    log.info("Folder: " + new File(ctx.getBean(Settings.class).getFolder()).getAbsolutePath());
    log.info("");

    while (true) {
      // Display menu
      log.info("Select a task:");
      log.info("1. Restore metadata");
      log.info("2. Duplication detection (requires metadata)");
      log.info("3. Duplication merge");
      log.info("4. Clean empty folders");
      log.info("");
      log.info("7. Quit");
      log.info("");

      // Get user input
      log.info("Enter your choice: ");
      int choice = scanner.nextInt();

      try {
        // Perform the selected task
        switch (choice) {
          case 1 -> ctx.getBean(MetadataService.class).restoreMetadata();
          case 2 -> ctx.getBean(DuplicateFinder.class).findDuplicates();
          case 3 -> ctx.getBean(DuplicateFinder.class).mergeDuplicates();
          case 4 -> ctx.getBean(OrganizerService.class).cleanEmptyFolders();
          // TODO: merge duplicates back (wenn nur 1 foto drin ist)

          case 7 -> {
            // Quit the loop if 7 is selected
            log.info("Exiting the program. Goodbye!");
            System.exit(0);
          }
          default -> log.info("Invalid choice. Please enter a valid option.");
        }
      } catch (Exception e) {
        log.info("Error!", e);
      }
    }
  }
}
