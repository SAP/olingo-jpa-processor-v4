package com.sap.olingo.jpa.processor.core.query;

import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.junit.jupiter.api.Tag;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.util.Assertions;

@Tag(Assertions.CB_ONLY_TEST)
class JPAExpandSubQueryTest extends JPAExpandQueryTest {

  @Override
  protected JPAExpandQuery createCut(final JPAInlineItemInfo item) throws ODataException {

    try {
      final var clazz = Class.forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper");
      final var constructor = clazz.getConstructor(EntityManagerFactory.class, JPAServiceDocument.class,
          ProcessorSqlPatternProvider.class);
      final var wrapper = constructor.newInstance(emf, helper.sd, null);
      final var createMethod = clazz.getMethod("createEntityManager");
      final var em = createMethod.invoke(wrapper);
      when(requestContext.getEntityManager()).thenReturn((EntityManager) em);
      return new JPAExpandSubQuery(OData.newInstance(), item, requestContext);
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
        | InvocationTargetException | InstantiationException | IllegalArgumentException e) {
      throw new ODataException(e);
    }
  }

}
