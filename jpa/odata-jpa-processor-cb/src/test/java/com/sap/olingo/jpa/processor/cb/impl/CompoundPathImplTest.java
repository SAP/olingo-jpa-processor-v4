package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class CompoundPathImplTest extends BuilderBaseTest {
  private From<Organization, Organization> root;
  private AliasBuilder ab;
  private JPAEntityType et;
  private CompoundPathImpl cut;
  private List<Path<Comparable<?>>> paths;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    et = sd.getEntity("Organizations");
    paths = new ArrayList<>();
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    cut = new CompoundPathImpl(paths);
  }

  @Test
  void testAsSQLOnePath() throws ODataJPAModelException {
    final String exp = "(E0.\"Address.StreetName\")";
    final JPAPath jpaPath = et.getPath("Address/StreetName");
    final PathImpl<Comparable<?>> sqlPath = new PathImpl<>(jpaPath, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath);

    assertEquals(exp, cut.asSQL(new StringBuilder()).toString());
  }

  @Test
  void testAsSQLTwoPath() throws ODataJPAModelException {
    final String exp = "(E0.\"Address.StreetName\", E0.\"ID\")";
    final JPAPath jpaPath1 = et.getPath("Address/StreetName");
    final PathImpl<Comparable<?>> sqlPath1 = new PathImpl<>(jpaPath1, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath1);

    final JPAPath jpaPath2 = et.getPath("ID");
    final PathImpl<Comparable<?>> sqlPath2 = new PathImpl<>(jpaPath2, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath2);

    assertEquals(exp, cut.asSQL(new StringBuilder()).toString());
  }

  @Test
  void testIsEmptyTrue() {
    assertTrue(cut.isEmpty());
  }

  @Test
  void testIsEmptyFalse() throws ODataJPAModelException {
    final JPAPath jpaPath2 = et.getPath("ID");
    final PathImpl<Comparable<?>> sqlPath2 = new PathImpl<>(jpaPath2, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath2);

    assertFalse(cut.isEmpty());
  }

  @Test
  void testGetFirstThrowsExceptionIfPathIsEmpty() {
    assertThrows(IllegalStateException.class, () -> cut.getFirst());
  }

  @Test
  void testGetFirstReturnsFirst() throws ODataJPAModelException {
    final JPAPath jpaPath1 = et.getPath("Address/StreetName");
    final PathImpl<Comparable<?>> sqlPath1 = new PathImpl<>(jpaPath1, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath1);

    final JPAPath jpaPath2 = et.getPath("ID");
    final PathImpl<Comparable<?>> sqlPath2 = new PathImpl<>(jpaPath2, Optional.of((PathImpl<?>) root), et, Optional
        .empty());
    paths.add(sqlPath2);
    assertEquals(sqlPath1, cut.getFirst());
  }
}
