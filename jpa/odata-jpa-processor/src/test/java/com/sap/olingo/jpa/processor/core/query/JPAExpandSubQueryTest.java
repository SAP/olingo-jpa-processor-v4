package com.sap.olingo.jpa.processor.core.query;

import static org.mockito.Mockito.when;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;

import com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper;

class JPAExpandSubQueryTest extends JPAExpandQueryTest {

  @SuppressWarnings("resource")
  @Override
  protected JPAExpandQuery createCut(final JPAInlineItemInfo item) throws ODataException {
    final var wrapper = new EntityManagerFactoryWrapper(emf, helper.sd, null);
    when(requestContext.getEntityManager()).thenReturn(wrapper.createEntityManager());
    return new JPAExpandSubQuery(OData.newInstance(), item, requestContext);
  }

}
