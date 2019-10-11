package ${package}.config;

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
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
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
      ObjectProvider<JtaTransactionManager> jtaTransactionManager,
      ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
    super(dataSource, properties, jtaTransactionManager, transactionManagerCustomizers);
  }

  @Override
  protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
    return new EclipseLinkJpaVendorAdapter();
  }

  @Override
  protected Map<String, Object> getVendorProperties() {
    // https://stackoverflow.com/questions/10769051/eclipselinkjpavendoradapter-instead-of-hibernatejpavendoradapter-issue
    HashMap<String, Object> map = new HashMap<>();
    map.put(PersistenceUnitProperties.WEAVING, "false");
    map.put(PersistenceUnitProperties.DDL_GENERATION, "none");
    map.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.FINE_LABEL);
    map.put(PersistenceUnitProperties.TRANSACTION_TYPE, "RESOURCE_LOCAL");
    return map;
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
      final EntityManagerFactoryBuilder builder, @Autowired final DataSource ds) {

    return builder
        .dataSource(ds)
        .packages(EntityTemplate.class)
        .persistenceUnit(punit)
        .properties(getVendorProperties())
        .jta(false)
        .build();
  }
}
