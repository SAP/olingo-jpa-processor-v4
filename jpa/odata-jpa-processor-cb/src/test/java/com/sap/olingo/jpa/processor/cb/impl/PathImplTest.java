package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public class PathImplTest extends BuilderBaseTest {
  private PathImpl<?> cut;
  private JPAEntityType et;
  protected From<Organization, Organization> root;
  private AliasBuilder ab;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    et = sd.getEntity("Organizations");
    final JPAPath path = et.getPath("Address/StreetName");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    cut = new PathImpl<>(path, Optional.of((PathImpl<?>) root), et, Optional.empty());
  }

  @Test
  public void testCutNotNull() {
    assertNotNull(cut);
  }

  @Test
  public void testAsSqlReturnsDBFilesName() {
    final StringBuilder act = new StringBuilder();
    cut.asSQL(act);
    assertEquals("E0.\"Address.StreetName\"", act.toString());
  }

  @Test
  public void testAsSqlReturnsDBFilesNameWithTableAlias() {
    root.alias("t0");
    final StringBuilder act = new StringBuilder();
    cut.asSQL(act);
    assertEquals("E0.\"Address.StreetName\"", act.toString());
  }

  @Test
  public void testReturnsComplexEmebbedId() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisionDescriptions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    final Path<Object> act = root.get("key").get("language");
    assertNotNull(act);
  }

  @Test
  public void testReturnsResolvedEmebbedId() throws ODataJPAModelException {
    et = sd.getEntity("AdministrativeDivisions");
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    final Path<Object> act = root.get("codePublisher");
    assertNotNull(act);
  }
}
