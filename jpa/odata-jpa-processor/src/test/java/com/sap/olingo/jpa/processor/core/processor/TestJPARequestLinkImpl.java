package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

public class TestJPARequestLinkImpl {
  private JPARequestLinkImpl cut;
  private JPAAssociationPath path;
  private List<JPAOnConditionItem> items;
  private JPAConversionHelper helper;

  @Before
  public void setUp() throws ODataJPAModelException {
    helper = new JPAConversionHelper();
    items = new ArrayList<JPAOnConditionItem>();
    path = mock(JPAAssociationPath.class);
    when(path.getJoinColumnsList()).thenReturn(items);
  }

  @Test
  public void testCreateMultipleStringKeys() throws ODataJPAModelException, ODataException {
    String link = "AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')";
    cut = new JPARequestLinkImpl(path, link, helper);

    items.add(createConditionItem("codePublisher", "CodePublisher", "codePublisher", "CodePublisher"));
    items.add(createConditionItem("parentCodeID", "ParentCodeID", "codeID", "CodeID"));
    items.add(createConditionItem("parentDivisionCode", "ParentDivisionCode", "divisionCode", "DivisionCode"));
    Map<String, Object> act = cut.getRelatedKeys();
    assertNotNull(act);
    assertEquals("DE100", act.get("divisionCode"));
    assertEquals("NUTS3", act.get("codeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  @Test
  public void testCreateMultipleStringValues() throws ODataJPAModelException, ODataException {
    String link = "AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')";
    cut = new JPARequestLinkImpl(path, link, helper);

    items.add(createConditionItem("codePublisher", "CodePublisher", "codePublisher", "CodePublisher"));
    items.add(createConditionItem("parentCodeID", "ParentCodeID", "codeID", "CodeID"));
    items.add(createConditionItem("parentDivisionCode", "ParentDivisionCode", "divisionCode", "DivisionCode"));
    Map<String, Object> act = cut.getValues();

    assertNotNull(act);
    assertEquals("DE100", act.get("parentDivisionCode"));
    assertEquals("NUTS3", act.get("parentCodeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  private JPAOnConditionItem createConditionItem(String leftInternalName, String leftExternalName,
      String rightInternalName, String rightExternalName) throws ODataJPAModelException {

    JPAOnConditionItem item = mock(JPAOnConditionItem.class);
    JPAPath leftPath = mock(JPAPath.class);
    JPAPath rightPath = mock(JPAPath.class);
    when(item.getLeftPath()).thenReturn(leftPath);
    when(item.getRightPath()).thenReturn(rightPath);

    JPAAttribute leftAttribute = mock(JPAAttribute.class);
    JPAAttribute rightAttribute = mock(JPAAttribute.class);
    when(leftPath.getLeaf()).thenReturn(leftAttribute);
    when(rightPath.getLeaf()).thenReturn(rightAttribute);

    when(leftAttribute.getInternalName()).thenReturn(leftInternalName);
    when(leftAttribute.getExternalName()).thenReturn(leftExternalName);
    when(rightAttribute.getInternalName()).thenReturn(rightInternalName);
    when(rightAttribute.getExternalName()).thenReturn(rightExternalName);
    when(leftAttribute.getEdmType()).thenReturn(EdmPrimitiveTypeKind.String);
    when(rightAttribute.getEdmType()).thenReturn(EdmPrimitiveTypeKind.String);

    return item;
  }
}
