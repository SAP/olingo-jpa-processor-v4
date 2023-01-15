package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterExpressionType;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterFunctions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictions.FilterExpressionRestrictionType;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateMethod;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateRestrictions;

@ExpandRestrictions(maxLevels = 2, nonExpandableProperties = { "roles" })
@FilterFunctions({ "at", "sum" })
@FilterRestrictions(requiredProperties = { "codePublisher,codeID" },
    filterExpressionRestrictions = {
        @FilterExpressionRestrictionType(
            allowedExpressions = FilterExpressionType.MULTI_VALUE, property = "codeID") })
@UpdateRestrictions(updateMethod = UpdateMethod.PATCH)
@CountRestrictions(nonCountableProperties = {},
    nonCountableNavigationProperties = { "parent", "children" })
@EdmIgnore
class IntermediateAnnotationConverterTest {

  private static final String CAPABILITIES_ANNOTATIONS = "annotations/Org.OData.Capabilities.V1.xml";
  private static final String CAPABILITIES_URL =
      "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Capabilities.V1.xml";
  private IntermediateReferences references;
  private IntermediateAnnotatable annotatable;

  @BeforeEach
  void setup() throws ODataJPAModelException, IOException {
    references = new IntermediateReferences();
    references.addReference(CAPABILITIES_URL, CAPABILITIES_ANNOTATIONS);
    annotatable = mock(IntermediateAnnotatable.class);
  }

  @Test
  void checkAnnotationWithoutAliaseIgnored() {
    final EdmIgnore ignore = this.getClass().getAnnotation(EdmIgnore.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, ignore, annotatable);
    assertFalse(act.isPresent());
  }

  @Test
  void checkAnnotationWithRecord() throws ODataJPAModelException {
    final JPAAssociationPath rolePath = mock(JPAAssociationPath.class);
    when(annotatable.convertStringToNavigationPath("roles")).thenReturn(rolePath);
    when(rolePath.getAlias()).thenReturn("Roles");

    final ExpandRestrictions er = this.getClass().getAnnotation(ExpandRestrictions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, er, annotatable);

    assertTrue(act.isPresent());
    assertEquals("ExpandRestrictions", act.get().getTerm());
    assertTrue(act.get().getExpression() instanceof CsdlRecord);
    final List<CsdlPropertyValue> actProperties = ((CsdlRecord) act.get().getExpression()).getPropertyValues();
    assertNotNull(actProperties);
    assertFalse(actProperties.isEmpty());
    for (final CsdlPropertyValue actProperty : actProperties) {
      if (actProperty.getProperty().equals("MaxLevels"))
        assertEquals("2", actProperty.getValue().asConstant().getValue());
      if (actProperty.getProperty().equals("Expandable"))
        assertEquals("true", actProperty.getValue().asConstant().getValue());
      if (actProperty.getProperty().equals("NonExpandableProperties"))
        assertEquals(1, actProperty.getValue().asDynamic().asCollection().getItems().size());
    }
  }

  @Test
  void checkAnnotationWithPrimitiveTypeCollection() {

    final FilterFunctions ff = this.getClass().getAnnotation(FilterFunctions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, ff, annotatable);

    assertTrue(act.isPresent());
    assertEquals("FilterFunctions", act.get().getTerm());
    assertEquals(2, ((CsdlCollection) act.get().getExpression()).getItems().size());

  }

  @Test
  void checkAnnotationWithRecordContainingEnum() {

    final UpdateRestrictions ur = this.getClass().getAnnotation(UpdateRestrictions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, ur, annotatable);
    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();
    final Optional<CsdlPropertyValue> updateMethod = actValues.stream()
        .filter(p -> p.getProperty().equals("UpdateMethod")).findFirst();
    assertTrue(updateMethod.isPresent());
    assertEquals("PATCH", updateMethod.get().getValue().asConstant().getValue());
  }

  @Test
  void checkAnnotationWithRecordContainingCollection() throws ODataJPAModelException {
    buildToPathResult();
    final FilterRestrictions fr = this.getClass().getAnnotation(FilterRestrictions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, fr, annotatable);

    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();

    final Optional<CsdlPropertyValue> requiredProperties = actValues.stream()
        .filter(p -> p.getProperty().equals("RequiredProperties")).findFirst();
    assertTrue(requiredProperties.isPresent());
    final CsdlCollection requiredPropertiesValue = (CsdlCollection) requiredProperties.get().getValue();
    assertEquals(2, requiredPropertiesValue.getItems().size());

    final Optional<CsdlPropertyValue> filterExpression = actValues.stream()
        .filter(p -> p.getProperty().equals("FilterExpressionRestrictions")).findFirst();
    assertTrue(filterExpression.isPresent());
    final CsdlCollection filterExpressionValue = (CsdlCollection) filterExpression.get().getValue();
    assertEquals(1, filterExpressionValue.getItems().size());

    final CsdlRecord filterExpressionItem = (CsdlRecord) filterExpressionValue.getItems().get(0);
    final Optional<CsdlPropertyValue> allowedExpressions = filterExpressionItem.getPropertyValues().stream()
        .filter(p -> p.getProperty().equals("AllowedExpressions")).findFirst();
    assertNotNull(allowedExpressions);
    assertTrue(allowedExpressions.isPresent());
    assertEquals("MultiValue", allowedExpressions.get().getValue().asConstant().getValue());
  }

  @Test
  void checkAnnotationWithPath() throws ODataJPAModelException {
    buildToPathResult();

    final FilterRestrictions fr = this.getClass().getAnnotation(FilterRestrictions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, fr, annotatable);

    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();

    final Optional<CsdlPropertyValue> requiredProperties = actValues.stream()
        .filter(p -> p.getProperty().equals("RequiredProperties")).findFirst();
    assertTrue(requiredProperties.isPresent());
    final CsdlCollection requiredPropertiesValue = (CsdlCollection) requiredProperties.get().getValue();

    assertTrue(requiredPropertiesValue.getItems().stream()
        .filter(item -> item.asDynamic().asPropertyPath().getValue().equals("CodePublisher"))
        .findFirst()
        .isPresent());

    assertTrue(requiredPropertiesValue.getItems().stream()
        .filter(item -> item.asDynamic().asPropertyPath().getValue().equals("CodeID"))
        .findFirst()
        .isPresent());

  }

  @Test
  void checkAnnotationWithNavigationPath() throws ODataJPAModelException {
    final JPAAssociationPath parentPath = mock(JPAAssociationPath.class);
    final JPAAssociationPath childrenPath = mock(JPAAssociationPath.class);

    when(annotatable.convertStringToNavigationPath("parent")).thenReturn(parentPath);
    when(annotatable.convertStringToNavigationPath("children")).thenReturn(childrenPath);

    when(parentPath.getAlias()).thenReturn("Parent");
    when(childrenPath.getAlias()).thenReturn("Children");

    final CountRestrictions cr = this.getClass().getAnnotation(CountRestrictions.class);
    final Optional<CsdlAnnotation> act = IntermediateAnnotationConverter.convert(references, cr, annotatable);

    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();

    final Optional<CsdlPropertyValue> requiredProperties = actValues.stream()
        .filter(p -> p.getProperty().equals("NonCountableNavigationProperties")).findFirst();
    assertTrue(requiredProperties.isPresent());
    final CsdlCollection requiredPropertiesValue = (CsdlCollection) requiredProperties.get().getValue();

    assertTrue(requiredPropertiesValue.getItems().stream()
        .filter(item -> item.asDynamic().asNavigationPropertyPath().getValue().equals("Parent"))
        .findFirst()
        .isPresent());

    assertTrue(requiredPropertiesValue.getItems().stream()
        .filter(item -> item.asDynamic().asNavigationPropertyPath().getValue().equals("Children"))
        .findFirst()
        .isPresent());
  }

  private void buildToPathResult() throws ODataJPAModelException {
    final JPAPath publisherPath = mock(JPAPath.class);
    final JPAPath idPath = mock(JPAPath.class);

    when(annotatable.convertStringToPath("codePublisher")).thenReturn(publisherPath);
    when(annotatable.convertStringToPath("codeID")).thenReturn(idPath);

    when(publisherPath.getAlias()).thenReturn("CodePublisher");
    when(idPath.getAlias()).thenReturn("CodeID");
  }
}
