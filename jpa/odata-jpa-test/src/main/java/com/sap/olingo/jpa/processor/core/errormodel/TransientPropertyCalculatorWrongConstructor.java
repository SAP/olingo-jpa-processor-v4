/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class TransientPropertyCalculatorWrongConstructor implements EdmTransientPropertyCalculator<String> {

  private final EntityManager em;
  private final String dummy;

  public TransientPropertyCalculatorWrongConstructor(final EntityManager em, final String dummy) {
    super();
    this.em = em;
    this.dummy = dummy;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  public String getDummy() {
    return dummy;
  }
}
