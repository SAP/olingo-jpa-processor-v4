package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class PathImplTest extends BuilderBaseTest {
  private PathImpl<?> cut;
  private JPAEntityType et;
  protected From<Organization, Organization> root;
  private AliasBuilder ab;

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<PathImpl> c = PathImpl.class;
    return Stream.of(
        arguments(c.getMethod("getModel")),
        arguments(c.getMethod("type")),
        arguments(c.getMethod("get", MapAttribute.class)),
        arguments(c.getMethod("get", PluralAttribute.class)),
        arguments(c.getMethod("get", SingularAttribute.class)));
  }

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    et = sd.getEntity("Organizations");
    final JPAPath path = et.getPath("Address/StreetName");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    cut = new PathImpl<>(path, Optional.of((PathImpl<?>) root), et, Optional.empty());
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {

    testNotImplemented(m, cut);
  }

  @Test
  void testCutNotNull() {
    assertNotNull(cut);
  }

  @Test
  void testToStringReturnsValue() {
    assertNotNull(cut.toString());
    assertTrue(cut.toString().startsWith("PathImpl [path="));
  }

  @Test
  void testGetParentPath() {
    assertNotNull(cut.getParentPath());
    assertEquals(root, cut.getParentPath());
  }

  @Test
  void testGetParentPathReturnsNullIfNotPresent() throws ODataJPAModelException {
    final JPAPath path = et.getPath("Address/StreetName");
    cut = new PathImpl<>(path, Optional.empty(), et, Optional.empty());
    assertNull(cut.getParentPath());
  }

  @Test
  void testGetPathList() {
    final List<JPAPath> act = cut.getPathList();
    assertFalse(act.isEmpty());
    assertEquals("Address/StreetName", act.get(0).getAlias());
  }

  @Test
  void testGetPathListThrowsExceptionOnMissingPath() {
    cut = new PathImpl<>(Optional.empty(), Optional.of((PathImpl<?>) root), et, Optional.empty());
    assertThrows(IllegalStateException.class, () -> cut.getPathList());
  }

  @Test
  void testAsSqlReturnsDBFilesName() {
    final StringBuilder act = new StringBuilder();
    cut.asSQL(act);
    assertEquals("E0.\"Address.StreetName\"", act.toString());
  }

  @Test
  void testAsSqlReturnsDBFilesNameWithTableAlias() {
    root.alias("t0");
    final StringBuilder act = new StringBuilder();
    cut.asSQL(act);
    assertEquals("E0.\"Address.StreetName\"", act.toString());
  }

  @Test
  void testGetReturnsComplexEmbedId() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisionDescriptions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    final Path<Object> act = root.get("key").get("language");
    assertNotNull(act);
  }

  @Test
  void testGetReturnsResolvedEmendedId() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    final Path<Object> act = root.get("codePublisher");
    assertNotNull(act);
  }

  @Test
  void testGetThrowsExceptionOnPrimitive() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    final Path<Object> publisher = root.get("codePublisher");
    assertThrows(IllegalArgumentException.class, () -> publisher.get("unknown"));
  }

  @Test
  void testGetThrowsExceptionOnUnKnownAttribute() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    assertThrows(IllegalArgumentException.class, () -> root.get("unknown"));
  }
}
