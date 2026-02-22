package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

class JPARequestLinkImplTest {
  private JPARequestLinkImpl cut;
  private JPAAssociationPath path;
  private List<JPAOnConditionItem> items;
  private JPAConversionHelper helper;
  private JPAAssociationAttribute pathLeaf;
  private JPAEntityType targetEntityType;

  @BeforeEach
  void setUp() throws ODataJPAModelException {
    helper = new JPAConversionHelper();
    items = new ArrayList<>();
    path = mock(JPAAssociationPath.class);
    pathLeaf = mock(JPAAssociationAttribute.class);
    when(path.getJoinColumnsList()).thenReturn(items);
    when(path.getTargetType()).thenReturn(targetEntityType);
  }

  @Test
  void testGetEntityType() {
    final String link = "AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')";
    cut = new JPARequestLinkImpl(path, link, helper);
    assertEquals(targetEntityType, cut.getEntityType());
  }

  @Test
  void testCreateMultipleStringKeysChildren() throws ODataException {
    createAdminDivisionChildrenRelation();
    final Map<String, Object> act = cut.getRelatedKeys();
    assertNotNull(act);
    assertEquals("DE100", act.get("divisionCode"));
    assertEquals("NUTS3", act.get("codeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  @Test
  void testCreateMultipleStringValuesChildren() throws ODataException {
    createAdminDivisionChildrenRelation();
    final Map<String, Object> act = cut.getValues();

    assertNotNull(act);
    assertEquals("DE100", act.get("parentDivisionCode"));
    assertEquals("NUTS3", act.get("parentCodeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  @Test
  void testCreateMultipleStringKeysParent() throws ODataException {
    createAdminDivisionParentRelation();
    final Map<String, Object> act = cut.getRelatedKeys();
    assertNotNull(act);
    assertEquals("DE100", act.get("divisionCode"));
    assertEquals("NUTS3", act.get("codeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  @Test
  void testCreateMultipleStringValuesParent() throws ODataException {
    createAdminDivisionParentRelation();
    final Map<String, Object> act = cut.getValues();

    assertNotNull(act);
    assertEquals("DE100", act.get("parentDivisionCode"));
    assertEquals("NUTS3", act.get("parentCodeID"));
    assertEquals("Eurostat", act.get("codePublisher"));
  }

  @Test
  void testCreateSingleStringKey() throws ODataException {
    final String link = "BusinessPartners('123456')";
    cut = new JPARequestLinkImpl(path, link, helper);

    completeJPAPath(false);

    final Map<String, Object> act = cut.getRelatedKeys();
    assertNotNull(act);
    assertEquals("123456", act.get("ID"));
  }

  @Test
  void testCreateSingleStringValue() throws ODataException {
    final String link = "BusinessPartners('123456')";
    cut = new JPARequestLinkImpl(path, link, helper);
    completeJPAPath(false);
    final Map<String, Object> act = cut.getValues();
    assertNotNull(act);
    assertEquals("123456", act.get("businessPartnerID"));
  }

  @Test
  void testCreateSingleStringKeyInverse() throws ODataException {
    final String link = "BusinessPartners('123456')";
    cut = new JPARequestLinkImpl(path, link, helper);

    completeJPAPath(true);

    final Map<String, Object> act = cut.getRelatedKeys();
    assertNotNull(act);
    assertEquals("123456", act.get("ID"));
  }

  @Test
  void testCreateSingleStringValueInverse() throws ODataException {
    final String link = "BusinessPartners('123456')";
    cut = new JPARequestLinkImpl(path, link, helper);
    completeJPAPath(true);
    final Map<String, Object> act = cut.getValues();
    assertNotNull(act);
    assertEquals("123456", act.get("businessPartnerID"));
  }

  @Test
  void testCreateSingleIntegerKeyJoinTable() throws ODataException {
    final String link = "JoinTarget(5)";
    cut = new JPARequestLinkImpl(path, link, helper);
    completeJPAPathJoinTable();
    final Map<String, Object> act = cut.getValues();
    assertNotNull(act);
    assertEquals(5, act.get("sourceID"));
    assertNotNull(cut.getRelatedKeys());
  }

  @Test
  void testCreateSingleStringValueThrowsException() throws ODataException {
    final String link = "BusinessPartners('123456')";
    when(path.getJoinColumnsList()).thenThrow(ODataJPAModelException.class);
    cut = new JPARequestLinkImpl(path, link, helper);

    assertThrows(ODataJPAProcessorException.class, () -> cut.getValues());
  }

  @Test
  void testCreateSingleStringKeyThrowsException() throws ODataException {
    final String link = "BusinessPartners('123456')";
    when(path.getJoinColumnsList()).thenThrow(ODataJPAModelException.class);
    cut = new JPARequestLinkImpl(path, link, helper);

    assertThrows(ODataJPAProcessorException.class, () -> cut.getRelatedKeys());
  }

  private void completeJPAPath(final Boolean inverted) throws ODataJPAModelException {
    final JPAEntityType targetEt = mock(JPAEntityType.class);
    final JPAEntityType sourceEt = mock(JPAEntityType.class);
    final JPAAttribute bupaKey = mock(JPAAttribute.class);
    final JPAAttribute roleKey1 = mock(JPAAttribute.class);
    final JPAAttribute roleKey2 = mock(JPAAttribute.class);
    final List<JPAAttribute> bupaKeys = new ArrayList<>();
    final List<JPAAttribute> roleKeys = new ArrayList<>();
    bupaKeys.add(bupaKey);
    roleKeys.add(roleKey1);
    roleKeys.add(roleKey2);
    when(bupaKey.getInternalName()).thenReturn("ID");
    when(bupaKey.getExternalName()).thenReturn("ID");
    when(roleKey1.getInternalName()).thenReturn("businessPartnerID");
    when(roleKey1.getExternalName()).thenReturn("BusinessPartnerID");
    when(roleKey2.getInternalName()).thenReturn("roleCategory");
    when(roleKey2.getExternalName()).thenReturn("BusinessPartnerRole");

    JPAPath leftPath;
    JPAPath rightPath;
    if (inverted) {
      items.add(createConditionItem("ID", "ID", "businessPartnerID", "BusinessPartnerID"));
      leftPath = createConditionPath("ID", "ID");
      rightPath = createConditionPath("businessPartnerID", "BusinessPartnerID");
    } else {
      items.add(createConditionItem("businessPartnerID", "BusinessPartnerID", "ID", "ID"));
      leftPath = createConditionPath("businessPartnerID", "BusinessPartnerID");
      rightPath = createConditionPath("ID", "ID");
    }
    when(path.getLeaf()).thenReturn(pathLeaf);
    when(pathLeaf.getInternalName()).thenReturn("businessPartner");
    when(path.getTargetType()).thenReturn(targetEt);
    when(path.getSourceType()).thenReturn(sourceEt);
    when(path.getLeftColumnsList()).thenReturn(List.of(leftPath));
    when(path.getRightColumnsList()).thenReturn(List.of(rightPath));
    when(targetEt.getKey()).thenReturn(bupaKeys);
    when(sourceEt.getKey()).thenReturn(roleKeys);

  }

  private void completeJPAPathJoinTable() throws ODataJPAModelException {
    final JPAEntityType targetEt = mock(JPAEntityType.class);
    final JPAEntityType sourceEt = mock(JPAEntityType.class);
    final JPAJoinTable joinTable = mock(JPAJoinTable.class);
    final JPAAttribute sourceKey = mock(JPAAttribute.class);
    final JPAAttribute targetKey = mock(JPAAttribute.class);
    final List<JPAAttribute> sourceKeys = new ArrayList<>();
    final List<JPAAttribute> targetKeys = new ArrayList<>();
    sourceKeys.add(sourceKey);
    targetKeys.add(targetKey);
    when(sourceKey.getInternalName()).thenReturn("sourceID");
    when(sourceKey.getExternalName()).thenReturn("SourceID");
    when(targetKey.getInternalName()).thenReturn("targetID");
    when(targetKey.getExternalName()).thenReturn("TargetID");

    when(path.getJoinColumnsList()).thenThrow(NullPointerException.class);
    when(path.getLeaf()).thenReturn(pathLeaf);
    when(pathLeaf.getInternalName()).thenReturn("oneToMany");
    when(pathLeaf.getEdmType()).thenReturn(EdmPrimitiveTypeKind.Int32);
    when(path.getTargetType()).thenReturn(targetEt);
    when(path.getSourceType()).thenReturn(sourceEt);
    when(path.getJoinTable()).thenReturn(joinTable);
    final var leftPath = createConditionPath("sourceID", "SourceID", EdmPrimitiveTypeKind.Int32);
    final var rightPath = createConditionPath("targetID", "TargetID", EdmPrimitiveTypeKind.Int32);
    when(joinTable.getLeftColumnsList()).thenReturn(List.of(leftPath));
    when(joinTable.getRightColumnsList()).thenReturn(List.of(rightPath));
    when(targetEt.getKey()).thenReturn(targetKeys);
    when(sourceEt.getKey()).thenReturn(sourceKeys);
  }

  private JPAOnConditionItem createConditionItem(final String leftInternalName, final String leftExternalName,
      final String rightInternalName, final String rightExternalName) throws ODataJPAModelException {

    final JPAOnConditionItem item = mock(JPAOnConditionItem.class);

    final JPAPath leftPath = createConditionPath(leftInternalName, leftExternalName);
    final JPAPath rightPath = createConditionPath(rightInternalName, rightExternalName);

    when(item.getLeftPath()).thenReturn(leftPath);
    when(item.getRightPath()).thenReturn(rightPath);

    return item;
  }

  private final JPAPath createConditionPath(final String internalName, final String externalName)
      throws ODataJPAModelException {
    return createConditionPath(internalName, externalName, EdmPrimitiveTypeKind.String);
  }

  private final JPAPath createConditionPath(final String internalName, final String externalName,
      final EdmPrimitiveTypeKind typeKind)
      throws ODataJPAModelException {
    final JPAPath conditionPath = mock(JPAPath.class);
    final JPAAttribute conditionAttribute = mock(JPAAttribute.class);
    when(conditionPath.getLeaf()).thenReturn(conditionAttribute);
    when(conditionAttribute.getInternalName()).thenReturn(internalName);
    when(conditionAttribute.getExternalName()).thenReturn(externalName);
    when(conditionAttribute.getEdmType()).thenReturn(typeKind);
    return conditionPath;
  }

  private void createAdminDivisionChildrenRelation() throws ODataJPAModelException {
    final String link = "AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')";
    cut = new JPARequestLinkImpl(path, link, helper);

    items.add(createConditionItem("codePublisher", "CodePublisher", "codePublisher", "CodePublisher"));
    items.add(createConditionItem("codeID", "CodeID", "parentCodeID", "ParentCodeID"));
    items.add(createConditionItem("divisionCode", "DivisionCode", "parentDivisionCode", "ParentDivisionCode"));
  }

  private void createAdminDivisionParentRelation() throws ODataJPAModelException {
    final String link = "AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')";
    cut = new JPARequestLinkImpl(path, link, helper);

    items.add(createConditionItem("codePublisher", "CodePublisher", "codePublisher", "CodePublisher"));
    items.add(createConditionItem("parentCodeID", "ParentCodeID", "codeID", "CodeID"));
    items.add(createConditionItem("parentDivisionCode", "ParentDivisionCode", "divisionCode", "DivisionCode"));
  }
}
