package ${package}.config;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CACHE_SHARED_DEFAULT;
import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MAX;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.WEAVING;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import ${package}.model.EntityTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class EclipseLinkJpaConfiguration extends JpaBaseConfiguration {
  @Value("${odata.jpa.punit_name}")
  private String punit;
  
  protected EclipseLinkJpaConfiguration(DataSource dataSource, JpaProperties properties,
      ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
    super(dataSource, properties, jtaTransactionManager);
  }

  @Override
  protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
    return new EclipseLinkJpaVendorAdapter();
  }

  @Override
  protected Map<String, Object> getVendorProperties() {
    // https://stackoverflow.com/questions/10769051/eclipselinkjpavendoradapter-instead-of-hibernatejpavendoradapter-issue
    HashMap<String, Object> jpaProperties = new HashMap<>();
    jpaProperties.put(WEAVING, "false");
    // No table generation by JPA 
    jpaProperties.put(DDL_GENERATION, "none");
    jpaProperties.put(LOGGING_LEVEL, SessionLog.FINE_LABEL);
    jpaProperties.put(TRANSACTION_TYPE, "RESOURCE_LOCAL");
    // do not cache entities locally, as this causes problems if multiple application instances are used
    jpaProperties.put(CACHE_SHARED_DEFAULT, "false");
    // You can also tweak your application performance by configuring your database connection pool.
    // https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#connectionpool
    jpaProperties.put(CONNECTION_POOL_MAX, 50);
    return jpaProperties;
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
      final EntityManagerFactoryBuilder builder, @Autowired final DataSource ds) {

    return builder
        .dataSource(ds)
        .packages(EntityTemplate.class)
        .properties(getVendorProperties())
        .jta(false)
        .build();
  }
}
