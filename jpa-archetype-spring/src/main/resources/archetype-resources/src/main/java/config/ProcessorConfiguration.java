package ${package}.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import ${package}.Template;

public class ProcessorConfiguration {
  @Value("${odata.jpa.punit_name}")
  private String punit;

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource")
  @Primary // makes this the default
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(EntityManagerFactoryBuilder builder) {
    return builder.dataSource(dataSource()).packages(Template.class).persistenceUnit(punit).build();
  }

}
