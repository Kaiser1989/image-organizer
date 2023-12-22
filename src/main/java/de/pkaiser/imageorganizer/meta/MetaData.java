package de.pkaiser.imageorganizer.meta;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
public class MetaData {

  private final Map<String, Entity> files = new ConcurrentHashMap<>();

  private boolean initialized = false;

  public void add(final Entity entity) {
    this.files.put(entity.getPath().toString(), entity);
  }

  public void markInitialized() {
    this.initialized = true;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class Entity {

    final Path path;

    final Instant date;
  }

}
