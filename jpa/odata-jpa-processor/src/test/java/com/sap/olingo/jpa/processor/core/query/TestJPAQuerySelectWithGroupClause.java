package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerWithGroups;
import com.sap.olingo.jpa.processor.core.util.SelectOptionDouble;
import com.sap.olingo.jpa.processor.core.util.TestGroupBase;
import com.sap.olingo.jpa.processor.core.util.UriInfoDouble;

public class TestJPAQuerySelectWithGroupClause extends TestGroupBase {

  @Test
  public void checkSelectAllWithoutGroupReturnsNotAssigned() throws ODataApplicationException, ODataJPAModelException {
    fillJoinTable(root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))).joinedPersistant(), root, Collections.emptyList());

    assertContains(selectClause, "ID");
    assertContainsNot(selectClause, "Country");
    assertContainsNot(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContainsNot(selectClause, "CreationDateTime");
  }

  @Test
  public void checkSelectAllWithOneGroupReturnsAlsoThose() throws ODataException, ODataJPAIllegalAccessException {
    root = emf.getCriteriaBuilder().createTupleQuery().from(BusinessPartnerWithGroups.class);
    fillJoinTable(root);
    final List<String> groups = new ArrayList<>();
    groups.add("Person");
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))).joinedPersistant(), root, groups);

    assertContains(selectClause, "ID");
    assertContains(selectClause, "Country");
    assertContains(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContainsNot(selectClause, "CreationDateTime");
  }

  @Test
  public void checkSelectAllWithTwoGroupReturnsAlsoThose() throws ODataException, ODataJPAIllegalAccessException {
    root = emf.getCriteriaBuilder().createTupleQuery().from(BusinessPartnerWithGroups.class);
    fillJoinTable(root);
    final List<String> groups = new ArrayList<>();
    groups.add("Person");
    groups.add("Company");
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))).joinedPersistant(), root, groups);

    assertContains(selectClause, "ID");
    assertContains(selectClause, "Country");
    assertContains(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContains(selectClause, "CreationDateTime");
  }

  @Test
  public void checkSelectTwoWithOneGroupReturnsAll() throws ODataApplicationException, ODataJPAModelException {
    root = emf.getCriteriaBuilder().createTupleQuery().from(BusinessPartnerWithGroups.class);
    fillJoinTable(root);
    final List<String> groups = new ArrayList<>();
    groups.add("Person");
    groups.add("Company");

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,CreationDateTime"))).joinedPersistant(), root, groups);

    assertContains(selectClause, "ID");
    assertContainsNot(selectClause, "Country");
    assertContainsNot(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContains(selectClause, "CreationDateTime");
  }

  @Test
  public void checkSelectTwoWithOneGroupReturnsOnlyID() throws ODataApplicationException, ODataJPAModelException {
    root = emf.getCriteriaBuilder().createTupleQuery().from(BusinessPartnerWithGroups.class);
    fillJoinTable(root);
    final List<String> groups = new ArrayList<>();
    groups.add("Test");

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,CreationDateTime"))).joinedPersistant(), root, groups);

    assertContains(selectClause, "ID");
    assertContainsNot(selectClause, "Country");
    assertContainsNot(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContainsNot(selectClause, "CreationDateTime");
  }

  @Test
  public void checkSelectTwoWithoutGroupReturnsOnlyID() throws ODataApplicationException, ODataJPAModelException {
    root = emf.getCriteriaBuilder().createTupleQuery().from(BusinessPartnerWithGroups.class);
    fillJoinTable(root);
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Test");

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,CreationDateTime"))).joinedPersistant(), root, Collections
            .emptyList());

    assertContains(selectClause, "ID");
    assertContainsNot(selectClause, "Country");
    assertContainsNot(selectClause, "CommunicationData/LandlinePhoneNumber");
    assertContainsNot(selectClause, "CreationDateTime");
  }

  private void assertContains(List<Selection<?>> selectClause, String alias) {
    for (Selection<?> selection : selectClause) {
      if (selection.getAlias().equals(alias))
        return;
    }
    fail(alias + " not found");
  }

  private void assertContainsNot(List<Selection<?>> selectClause, String alias) {
    for (Selection<?> selection : selectClause) {
      if (selection.getAlias().equals(alias))
        fail(alias + " was found, but was not expected");
    }
  }
}
