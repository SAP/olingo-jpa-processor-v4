package com.sap.olingo.jpa.metadata.odata.v4.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.CsdlDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.CsdlDocumentReader;
import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.ODataJPAVocabulariesException;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateMethod;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Immutable;

@ExpandRestrictions(maxLevels = 2, nonExpandableProperties = { "roles" })
//@FilterFunctions({ "at", "sum" })
@FilterRestrictions(requiredProperties = { "codePublisher,codeID" })
//    filterExpressionRestrictions = {
//        @FilterExpressionRestrictionType(
//            allowedExpressions = FilterExpressionType.MULTI_VALUE, property = "codeID") })
@UpdateRestrictions(updateMethod = UpdateMethod.PATCH)
@CountRestrictions(nonCountableProperties = {},
    nonCountableNavigationProperties = { "parent", "children" })

@EdmIgnore
class JavaAnnotationConverterTest {

  private JPAReferences references;
  private ODataAnnotatable annotatable;
  @Immutable
  private CsdlDocument vocabulary;
  private JavaAnnotationConverter cut;

  @BeforeEach
  void setup() throws IOException, ODataJPAVocabulariesException {
    vocabulary = new CsdlDocumentReader().readFromResource(JavaBasedCapabilitiesAnnotationsProvider.PATH,
        Charset.defaultCharset());
    references = new References(vocabulary);
    annotatable = mock(ODataAnnotatable.class);
    cut = new JavaAnnotationConverter();
  }

  @Test
  void checkAnnotationWithoutAliasIgnored() {
    final EdmIgnore ignore = this.getClass().getAnnotation(EdmIgnore.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, ignore, annotatable);
    assertFalse(act.isPresent());
  }

  @Test
  void checkAnnotationWithPrimitiveType() throws ODataJPAVocabulariesException, IOException, NoSuchFieldException,
      SecurityException {
    vocabulary = new CsdlDocumentReader().readFromResource(JavaBasedCoreAnnotationsProvider.PATH,
        Charset.defaultCharset());
    references = new References(vocabulary);

    final Immutable immutable = this.getClass().getDeclaredField("vocabulary").getAnnotation(Immutable.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, immutable, annotatable);

    assertTrue(act.isPresent());
    assertEquals(JavaBasedCoreAnnotationsProvider.NAMESPACE + "." + "Immutable", act.get().getTerm());
  }

  @Test
  void checkAnnotationWithRecord() throws ODataPathNotFoundException {
    final ODataNavigationPath rolePath = mock(ODataNavigationPath.class);
    when(annotatable.convertStringToNavigationPath("roles")).thenReturn(rolePath);
    when(rolePath.getPathAsString()).thenReturn("Roles");

    final ExpandRestrictions expandRestrictions = this.getClass().getAnnotation(ExpandRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, expandRestrictions, annotatable);

    assertTrue(act.isPresent());
    assertEquals(JavaBasedCapabilitiesAnnotationsProvider.NAMESPACE + ".ExpandRestrictions", act.get().getTerm());
    assertTrue(act.get().getExpression() instanceof CsdlRecord);
    assertNotNull(new FullQualifiedName(act.get().getTerm()));
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

//  @Test
//  void checkAnnotationWithPrimitiveTypeCollection() {
//
//    final FilterFunctions ff = this.getClass().getAnnotation(FilterFunctions.class);
//    final Optional<CsdlAnnotation> act = cut.convert(references, ff, annotatable);
//
//    assertTrue(act.isPresent());
//    assertEquals(JavaBasedCapabilitiesAnnotationsProvider.NAMESPACE + "." + "FilterFunctions", act.get().getTerm());
//    assertEquals(2, ((CsdlCollection) act.get().getExpression()).getItems().size());
//  }

  @Test
  void checkAnnotationWithRecordContainingEnum() {

    final UpdateRestrictions updateRestrictions = this.getClass().getAnnotation(UpdateRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, updateRestrictions, annotatable);
    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();
    final Optional<CsdlPropertyValue> updateMethod = actValues.stream()
        .filter(p -> p.getProperty().equals("UpdateMethod")).findFirst();
    assertTrue(updateMethod.isPresent());
    assertEquals("PATCH", updateMethod.get().getValue().asConstant().getValue());
  }

  @Test
  void checkAnnotationWithRecordContainingCollection() throws ODataPathNotFoundException {

    buildToPathResult();
    final FilterRestrictions filterRestrictions = this.getClass().getAnnotation(FilterRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, filterRestrictions, annotatable);

    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();

    final Optional<CsdlPropertyValue> requiredProperties = actValues.stream()
        .filter(p -> p.getProperty().equals("RequiredProperties")).findFirst();
    assertTrue(requiredProperties.isPresent());
    final CsdlCollection requiredPropertiesValue = (CsdlCollection) requiredProperties.get().getValue();
    assertEquals(2, requiredPropertiesValue.getItems().size());

//    final Optional<CsdlPropertyValue> filterExpression = actValues.stream()
//        .filter(p -> p.getProperty().equals("FilterExpressionRestrictions")).findFirst();
//    assertTrue(filterExpression.isPresent());
//    final CsdlCollection filterExpressionValue = (CsdlCollection) filterExpression.get().getValue();
//    assertEquals(1, filterExpressionValue.getItems().size());

//    final CsdlRecord filterExpressionItem = (CsdlRecord) filterExpressionValue.getItems().get(0);
//    final Optional<CsdlPropertyValue> allowedExpressions = filterExpressionItem.getPropertyValues().stream()
//        .filter(p -> p.getProperty().equals("AllowedExpressions")).findFirst();
//    assertNotNull(allowedExpressions);
//    assertTrue(allowedExpressions.isPresent());
//    assertEquals("MultiValue", allowedExpressions.get().getValue().asConstant().getValue());
  }

  @Test
  void checkAnnotationWithPath() throws ODataPathNotFoundException {
    buildToPathResult();

    final FilterRestrictions filterRestrictions = this.getClass().getAnnotation(FilterRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, filterRestrictions, annotatable);

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
  void checkAnnotationPathExpressionReturnsNullOnException() throws ODataPathNotFoundException {
    buildToPathResult();
    when(annotatable.convertStringToPath(any())).thenThrow(ODataPathNotFoundException.class);
    final FilterRestrictions filterRestrictions = this.getClass().getAnnotation(FilterRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, filterRestrictions, annotatable);

    assertTrue(act.isPresent());
    final List<CsdlPropertyValue> actValues = act.get().getExpression().asDynamic().asRecord().getPropertyValues();
    final Optional<CsdlPropertyValue> requiredProperties = actValues.stream()
        .filter(p -> p.getProperty().equals("RequiredProperties")).findFirst();
    assertTrue(requiredProperties.isPresent());
    assertTrue(requiredProperties.get().getValue().asDynamic().asCollection().getItems().isEmpty());
  }

  @Test
  void checkAnnotationWithNavigationPath() throws ODataPathNotFoundException {
    final ODataNavigationPath parentPath = mock(ODataNavigationPath.class);
    final ODataNavigationPath childrenPath = mock(ODataNavigationPath.class);

    when(annotatable.convertStringToNavigationPath("parent")).thenReturn(parentPath);
    when(annotatable.convertStringToNavigationPath("children")).thenReturn(childrenPath);

    when(parentPath.getPathAsString()).thenReturn("Parent");
    when(childrenPath.getPathAsString()).thenReturn("Children");

    final CountRestrictions countRestrictions = this.getClass().getAnnotation(CountRestrictions.class);
    final Optional<CsdlAnnotation> act = cut.convert(references, countRestrictions, annotatable);

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

  private void buildToPathResult() throws ODataPathNotFoundException {
    final ODataPropertyPath publisherPath = mock(ODataPropertyPath.class);
    final ODataPropertyPath idPath = mock(ODataPropertyPath.class);

    when(annotatable.convertStringToPath("codePublisher")).thenReturn(publisherPath);
    when(annotatable.convertStringToPath("codeID")).thenReturn(idPath);

    when(publisherPath.getPathAsString()).thenReturn("CodePublisher");
    when(idPath.getPathAsString()).thenReturn("CodeID");
  }
}
