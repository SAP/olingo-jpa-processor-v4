/**
 *
 */
package com.sap.olingo.jpa.processor.core.testobjects;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 *
 * @author Oliver Grande
 * @since 1.0.3
 * 20.05.2021
 */
public class ThreeParameterTransientPropertyConverter implements EdmTransientPropertyCalculator<String> {
  private final EntityManager em;
  private final JPAHttpHeaderMap header;
  private final JPARequestParameterMap parameter;

  public ThreeParameterTransientPropertyConverter(final EntityManager em, final JPAHttpHeaderMap header,
      final JPARequestParameterMap parameter) {
    super();
    this.em = em;
    this.header = header;
    this.parameter = parameter;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  public JPAHttpHeaderMap getHeader() {
    return header;
  }

  public JPARequestParameterMap getParameter() {
    return parameter;
  }
}
