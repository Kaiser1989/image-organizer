package de.pkaiser.imageorganizer.meta;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MetaDataConfiguration {

  @Bean
  public MetaData produceMetaData() {
    return new MetaData();
  }

}
