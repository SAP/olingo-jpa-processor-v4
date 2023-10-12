/**
 *
 */
package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class TwoParameterTransientPropertyConverter implements EdmTransientPropertyCalculator<String> {
  private final EntityManager em;
  private final Map<String, List<String>> header;

  public TwoParameterTransientPropertyConverter(final EntityManager em, final JPAHttpHeaderMap header) {
    super();
    this.em = em;
    this.header = header;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  public Map<String, List<String>> getHeader() {
    return header;
  }
}
