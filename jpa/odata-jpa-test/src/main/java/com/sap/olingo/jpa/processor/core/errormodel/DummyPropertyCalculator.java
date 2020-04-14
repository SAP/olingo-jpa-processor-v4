/**
 * 
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class DummyPropertyCalculator implements EdmTransientPropertyCalculator<String>  {

  private final EntityManager em;

  public DummyPropertyCalculator(final EntityManager em) {
    super();
    this.em = em;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public List<String> calculateCollectionProperty(Tuple row) {
    return Arrays.asList("Hello","World");
  }

}
