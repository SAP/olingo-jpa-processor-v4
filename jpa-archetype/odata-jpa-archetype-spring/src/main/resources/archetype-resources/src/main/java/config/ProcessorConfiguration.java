package ${package}.config;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;

@Configuration
public class ProcessorConfiguration {
  @Value("${odata.jpa.punit_name}")
  private String punit;
  @Value("${odata.jpa.root_packages}") 
  private String rootPackages;
  
  @Bean
  public JPAODataCRUDContextAccess sessionContext(@Autowired final EntityManagerFactory emf) throws ODataException {

    return JPAODataServiceContext.with()
        .setPUnit(punit)
        .setEntityManagerFactory(emf)
        .setTypePackage(rootPackages)
        .setRequestMappingPath("${punit}/v1")
        .build();
  }
}