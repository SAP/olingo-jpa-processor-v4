package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAEmptyDebugger;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;

class JPAJoinQueryTest extends TestQueryBase {
  private CriteriaBuilder cb;

  private EntityManager em;
  private JPAODataRequestContextAccess localContext;
  private JPAHttpHeaderMap headerMap;
  private JPARequestParameterMap parameterMap;

  @Override
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    em = mock(EntityManager.class);
    cb = spy(emf.getCriteriaBuilder());

    localContext = mock(JPAODataRequestContextAccess.class);
    headerMap = mock(JPAHttpHeaderMap.class);
    parameterMap = mock(JPARequestParameterMap.class);

    buildUriInfo("BusinessPartners", "BusinessPartner");
    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType(BusinessPartner.class);
    createHeaders();

    when(localContext.getUriInfo()).thenReturn(uriInfo);
    when(localContext.getEntityManager()).thenReturn(em);
    when(localContext.getEdmProvider()).thenReturn(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages));
    when(localContext.getDebugger()).thenReturn(new JPAEmptyDebugger());
    when(localContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    when(localContext.getHeader()).thenReturn(headerMap);
    when(localContext.getRequestParameter()).thenReturn(parameterMap);
    when(em.getCriteriaBuilder()).thenReturn(cb);

    cut = new JPAJoinQuery(null, localContext);
  }

  @Test
  void testDerivedTypeRequestedTrueTwoLevels() {

    final var rootType = mock(JPAStructuredType.class);
    final var baseType = mock(JPAStructuredType.class);
    final var potentialSubType = mock(JPAStructuredType.class);

    when(potentialSubType.getBaseType()).thenReturn(baseType);
    when(baseType.getBaseType()).thenReturn(rootType);

    assertTrue(cut.derivedTypeRequested(rootType, potentialSubType));
  }

  @Test
  void testDerivedTypeRequestedTrue() {

    final var baseType = mock(JPAStructuredType.class);
    final var potentialSubType = mock(JPAStructuredType.class);

    when(potentialSubType.getBaseType()).thenReturn(baseType);

    assertTrue(cut.derivedTypeRequested(baseType, potentialSubType));
  }

  @Test
  void testDerivedTypeRequestedFalseNoBaseType() {

    final var baseType = mock(JPAStructuredType.class);
    final var potentialSubType = mock(JPAStructuredType.class);

    when(potentialSubType.getBaseType()).thenReturn(null);

    assertFalse(cut.derivedTypeRequested(baseType, potentialSubType));
  }

  @Test
  void testDerivedTypeRequestedFalseOtherBaseType() {

    final var baseType = mock(JPAStructuredType.class);
    final var baseType2 = mock(JPAStructuredType.class);
    final var potentialSubType = mock(JPAStructuredType.class);

    when(potentialSubType.getBaseType()).thenReturn(baseType2);

    assertFalse(cut.derivedTypeRequested(baseType, potentialSubType));
  }

  @Test
  void testBuildEntityPathListRethrowsException() throws ODataApplicationException, ODataJPAModelException {
    final var jpaEntityType = mock(JPAEntityType.class);
    when(jpaEntityType.getPathList()).thenThrow(ODataJPAModelException.class);
    assertThrows(ODataApplicationException.class, () -> cut.buildEntityPathList(jpaEntityType));
  }
}
