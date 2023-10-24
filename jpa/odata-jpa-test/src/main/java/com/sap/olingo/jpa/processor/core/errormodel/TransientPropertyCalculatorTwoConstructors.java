/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande<br>
 * Created: 17.03.2020
 *
 */
public class TransientPropertyCalculatorTwoConstructors implements EdmTransientPropertyCalculator<String> {

  private final EntityManager em;

  public TransientPropertyCalculatorTwoConstructors() {
    super();
    this.em = null;
  }

  public TransientPropertyCalculatorTwoConstructors(final EntityManager em) {
    super();
    this.em = em;
  }

  public EntityManager getEntityManager() {
    return em;
  }
}
