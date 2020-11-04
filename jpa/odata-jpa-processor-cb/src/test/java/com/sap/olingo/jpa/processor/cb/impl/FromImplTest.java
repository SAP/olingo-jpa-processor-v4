package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class FromImplTest extends BuilderBaseTest {
  private From<Organization, Organization> cut;
  private AliasBuilder ab;
  private CriteriaBuilderImpl cb;

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<FromImpl> c = FromImpl.class;
    return Stream.of(
        arguments(c.getMethod("fetch", String.class)),
        arguments(c.getMethod("fetch", String.class, JoinType.class)),
        arguments(c.getMethod("fetch", SingularAttribute.class)),
        arguments(c.getMethod("fetch", SingularAttribute.class, JoinType.class)),
        arguments(c.getMethod("fetch", PluralAttribute.class)),
        arguments(c.getMethod("fetch", PluralAttribute.class, JoinType.class)),
        arguments(c.getMethod("join", SetAttribute.class)),
        arguments(c.getMethod("join", SetAttribute.class, JoinType.class)),
        arguments(c.getMethod("join", MapAttribute.class)),
        arguments(c.getMethod("join", MapAttribute.class, JoinType.class)),
        arguments(c.getMethod("join", ListAttribute.class)),
        arguments(c.getMethod("join", ListAttribute.class, JoinType.class)),
        arguments(c.getMethod("join", CollectionAttribute.class)),
        arguments(c.getMethod("join", CollectionAttribute.class, JoinType.class)),
        arguments(c.getMethod("joinCollection", String.class)),
        arguments(c.getMethod("joinCollection", String.class, JoinType.class)),
        arguments(c.getMethod("joinSet", String.class)),
        arguments(c.getMethod("joinSet", String.class, JoinType.class)),
        arguments(c.getMethod("joinMap", String.class)),
        arguments(c.getMethod("joinMap", String.class, JoinType.class)),
        arguments(c.getMethod("joinList", String.class)),
        arguments(c.getMethod("joinList", String.class, JoinType.class)),
        arguments(c.getMethod("join", SingularAttribute.class)),
        arguments(c.getMethod("isCorrelated")),
        arguments(c.getMethod("getCorrelationParent")));
  }

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    final JPAEntityType et = sd.getEntity(Organization.class);
    cb = mock(CriteriaBuilderImpl.class);
    ab = new AliasBuilder();
    when(cb.getServiceDocument()).thenReturn(sd);
    cut = new FromImpl<>(et, ab, cb);
  }

  @Test
  public void testCutNotNull() {
    assertNotNull(cut);
  }

  @Test
  public void testAsSqlReturnsDBTableNameWithoutAlias() {
    final StringBuilder act = new StringBuilder();
    ((SqlConvertible) cut).asSQL(act);
    assertEquals("\"OLINGO\".\"BusinessPartner\" E0", act.toString());
  }

  @Test
  public void testAsSqlReturnsDBTableNameWithAlias() {
    final StringBuilder act = new StringBuilder();
    cut.alias("t0");
    ((SqlConvertible) cut).asSQL(act);
    assertEquals("\"OLINGO\".\"BusinessPartner\" E0", act.toString());
  }

  @Test
  public void testGetByNameReturnsPathToAttribute() {
    final Path<String> act = cut.get("iD");
    assertNotNull(act);
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  public void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {
    InvocationTargetException e;
    if (m.getParameterCount() >= 1) {
      final Class<?>[] params = m.getParameterTypes();
      final List<Object> paramValues = new ArrayList<>(m.getParameterCount());
      for (int i = 0; i < m.getParameterCount(); i++) {
        if (params[i] == char.class)
          paramValues.add(' ');
        else
          paramValues.add(null);
      }
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut, paramValues.toArray()));
    } else {
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut));
    }
    assertTrue(e.getCause() instanceof NotImplementedException);
  }

  @Test
  public void testCreateJoinByNavigationAttributeName() {
    final String exp =
        "\"OLINGO\".\"BusinessPartner\" E0 INNER JOIN \"OLINGO\".\"BusinessPartnerRole\" E1 ON (E0.\"ID\" = E1.\"BusinessPartnerID\")";
    final StringBuilder statement = new StringBuilder();
    final Join<?, ?> act = cut.join("roles");
    assertNotNull(act);
    assertEquals(1, cut.getJoins().size());
    assertTrue(cut.getJoins().contains(act));
    ((SqlConvertible) cut).asSQL(statement);
    assertEquals(exp, statement.toString());
  }

  @Test
  public void testCreateJoinByComplexAttributeName() {
    final String exp = "\"OLINGO\".\"BusinessPartner\" E0";
    final StringBuilder statement = new StringBuilder();
    final Join<?, ?> act = cut.join("address");
    assertNotNull(act);
    assertEquals(1, cut.getJoins().size());
    ((SqlConvertible) cut).asSQL(statement);
    assertEquals(exp, statement.toString());
  }

  @Test
  public void testCreateJoinByDescriptionAttributeName() {
    final String exp =
        "\"OLINGO\".\"BusinessPartner\" E0 "
            + "LEFT OUTER JOIN \"OLINGO\".\"AdministrativeDivisionDescription\" E1 "
            + "ON (E0.\"Country\" = E1.\"DivisionCode\")";
    final StringBuilder statement = new StringBuilder();
    final Join<?, ?> act = cut.join("locationName");
    assertNotNull(act);
    assertEquals(1, cut.getJoins().size());
    ((SqlConvertible) cut).asSQL(statement);
    assertEquals(exp, statement.toString());
  }

  @Test
  public void testCreateJoinByDescriptionViaComplexAttributeName() {
    final String exp =
        "\"OLINGO\".\"BusinessPartner\" E0 "
            + "LEFT OUTER JOIN \"OLINGO\".\"CountryDescription\" E2 "
            + "ON (E0.\"Address.Country\" = E2.\"ISOCode\")";
    final StringBuilder statement = new StringBuilder();
    final Join<?, ?> act = cut.join("address").join("countryName");
    assertNotNull(act);
    assertEquals(1, cut.getJoins().size());
    ((SqlConvertible) cut).asSQL(statement);
    assertEquals(exp, statement.toString());
  }

  // "Organizations?$select=Name1&$filter=SupportEngineers/any(d:d/LastName eq 'Doe')");
  @Test
  public void testCreateJoinViaJoinTable() {
    final String exp =
        "\"OLINGO\".\"BusinessPartner\" E0 "
            + "INNER JOIN (\"OLINGO\".\"SupportRelationship\" E1 "
            + "INNER JOIN \"OLINGO\".\"BusinessPartner\" E2 "
            + "ON (E1.\"PersonID\" = E2.\"ID\")) "
            + "ON (E0.\"ID\" = E1.\"OrganizationID\")";
    final StringBuilder statement = new StringBuilder();
    final Join<?, ?> act = cut.join("supportEngineers");
    assertNotNull(act);
    assertEquals(1, cut.getJoins().size());
    ((SqlConvertible) cut).asSQL(statement);
    assertEquals(exp, statement.toString());
  }

  @Test
  public void testGetJavaType() {
    assertEquals(Organization.class, cut.getJavaType());
  }

  @Test
  public void testAsWithSuperType() {
    assertNotNull(cut.as(BusinessPartner.class));
  }

  @Test
  public void testAsWithSubType() throws ODataJPAModelException {
    final JPAEntityType et = sd.getEntity(BusinessPartner.class);
    cut = new FromImpl<>(et, ab, cb);

    final Expression<Organization> act = cut.as(Organization.class);

    assertNotNull(act);
  }

  @Test
  public void testAsUnknownTypeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> cut.as(this.getClass()));
  }

  @Test
  public void testAsRethrowsModelöException() throws ODataJPAModelException {
    when(sd.getEntity(Person.class)).thenThrow(ODataJPAModelException.class);
    assertThrows(IllegalArgumentException.class, () -> cut.as(Person.class));
  }

  @Test
  public void testJoinThrowsExceptionOnUnknowAttribute() {
    assertThrows(IllegalArgumentException.class, () -> cut.join("dummy"));
  }
}
