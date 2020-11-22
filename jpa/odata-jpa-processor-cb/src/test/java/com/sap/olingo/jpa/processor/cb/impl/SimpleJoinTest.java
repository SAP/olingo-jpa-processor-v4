package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.AbstractJoinImp.RawPath;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;

class SimpleJoinTest extends BuilderBaseTest {
  private SimpleJoin<BusinessPartner, BusinessPartnerRole> cut;
  private AbstractJoinImp<BusinessPartner, BusinessPartnerRole>.RawPath<Long> rawCut;
  private AliasBuilder ab;
  private CriteriaBuilderImpl cb;

  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<?> c = RawPath.class;
    return Stream.of(
        arguments(c.getMethod("getModel")),
        arguments(c.getMethod("getParentPath")),
        arguments(c.getMethod("get", SingularAttribute.class)),
        arguments(c.getMethod("get", PluralAttribute.class)),
        arguments(c.getMethod("get", MapAttribute.class)),
        arguments(c.getMethod("get", String.class)),
        arguments(c.getMethod("type")));
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    final ProcessorCriteriaQuery<Tuple> q = cb.createTupleQuery();
    final JPAAssociationPath path = sd.getEntity(BusinessPartner.class).getAssociationPath("Roles");
    cut = new SimpleJoin<>(path, JoinType.INNER, q.from(BusinessPartner.class), ab, cb);

    final JPAJoinColumn joinColumn = mock(JPAJoinColumn.class);
    when(joinColumn.getName()).thenReturn("One");
    when(joinColumn.getReferencedColumnName()).thenReturn("Two");
    cut.createOn(Arrays.asList(joinColumn));
    rawCut = (AbstractJoinImp<BusinessPartner, BusinessPartnerRole>.RawPath<Long>) ((PredicateImpl) ((PredicateImpl) cut
        .getOn()).expressions.get(1)).expressions.get(1);
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {
    testNotImplemented(m, rawCut);
  }

  @Test
  void testGetOn() {
    assertNotNull(cut.getOn());
  }

  @Test
  void testGetJoinType() {
    assertEquals(JoinType.INNER, cut.getJoinType());
  }

  @Test
  void testGetAttributeNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.getAttribute());
  }

}
