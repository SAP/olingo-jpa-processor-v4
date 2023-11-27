package com.sap.olingo.jpa.metadata.odata.v4.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataVocabularyReadException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceAccess;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceList;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictions;

@ExpandRestrictions(maxLevels = 2, nonExpandableProperties = { "roles" })
class JavaBasedODataAnnotationsProviderTest {
  JavaBasedCapabilitiesAnnotationsProvider cut;
  ODataAnnotatable annotatable;
  JPAReferences references;
  List<CsdlTerm> terms;
  CsdlTerm term;
  JavaAnnotationConverter converter;

  @BeforeEach
  void setup() {
    references = mock(JPAReferences.class);
    annotatable = mock(ODataAnnotatable.class);
    terms = new ArrayList<>();
    term = mock(CsdlTerm.class);
    converter = mock(JavaAnnotationConverter.class);
    cut = new JavaBasedCapabilitiesAnnotationsProvider(converter);
  }

  @Test
  void checkGetAnnotationsNPEOnReferenceNull() {
    assertThrows(NullPointerException.class, () -> cut.getAnnotations(Applicability.ENTITY_SET, annotatable, null));
  }

  @Test
  void checkGetAnnotationsNPEOnAppliesToNull() {
    terms.add(term);
    when(references.getTerms(any(), any())).thenReturn(terms);
    assertThrows(NullPointerException.class, () -> cut.getAnnotations(null, annotatable, references));
  }

  @Test
  void checkGetAnnotationsNPEAnnotatableToNull() {
    terms.add(term);
    when(references.getTerms(any(), any())).thenReturn(terms);
    assertThrows(NullPointerException.class, () -> cut.getAnnotations(Applicability.ENTITY_SET, null, references));
  }

  @Test
  void checkGetAnnotationsReturnsTheFoundOne() {
    final ExpandRestrictions expandAnnotation = this.getClass().getAnnotation(ExpandRestrictions.class);
    final CsdlAnnotation converted = mock(CsdlAnnotation.class);
    terms.add(term);
    when(references.getTerms(any(), any())).thenReturn(terms);
    when(term.getName()).thenReturn("ExpandTest");
    when(annotatable.javaAnnotations(any())).thenReturn(Collections.singletonMap("ExpandTest", expandAnnotation));
    when(converter.convert(references, expandAnnotation, annotatable)).thenReturn(Optional.of(converted));

    final Collection<CsdlAnnotation> act = cut.getAnnotations(Applicability.ENTITY_SET, annotatable, references);
    assertEquals(1, act.size());
    assertEquals(converted, act.stream().findFirst().get());
  }

  @Test
  void checkGetAnnotationsReturnsTheFoundOneAtAnnotatable() {
    final ExpandRestrictions expandAnnotation = this.getClass().getAnnotation(ExpandRestrictions.class);
    final CsdlAnnotation converted = mock(CsdlAnnotation.class);
    final CsdlTerm secondTerm = mock(CsdlTerm.class);
    terms.add(term);
    terms.add(secondTerm);
    when(references.getTerms(any(), any())).thenReturn(terms);
    when(term.getName()).thenReturn("ExpandTest");
    when(annotatable.javaAnnotations(any())).thenReturn(Collections.singletonMap("ExpandTest", expandAnnotation));
    when(converter.convert(references, expandAnnotation, annotatable)).thenReturn(Optional.of(converted));

    final Collection<CsdlAnnotation> act = cut.getAnnotations(Applicability.ENTITY_SET, annotatable, references);
    assertEquals(1, act.size());
    assertEquals(converted, act.stream().findFirst().get());
  }

  @Test
  void checkGetAnnotationsReturnsEmptyIfNotConverter() {
    final ExpandRestrictions expandAnnotation = this.getClass().getAnnotation(ExpandRestrictions.class);
    final CsdlTerm secondTerm = mock(CsdlTerm.class);
    terms.add(term);
    terms.add(secondTerm);
    when(references.getTerms(any(), any())).thenReturn(terms);
    when(term.getName()).thenReturn("ExpandTest");
    when(annotatable.javaAnnotations(any())).thenReturn(Collections.singletonMap("ExpandTest", expandAnnotation));
    when(converter.convert(references, expandAnnotation, annotatable)).thenReturn(Optional.empty());

    final Collection<CsdlAnnotation> act = cut.getAnnotations(Applicability.ENTITY_SET, annotatable, references);
    assertTrue(act.isEmpty());
  }

  @Test
  void checkGetAnnotationsReturnsEmptyIfNotAssigned() {
    final ExpandRestrictions expandAnnotation = this.getClass().getAnnotation(ExpandRestrictions.class);
    final CsdlTerm secondTerm = mock(CsdlTerm.class);
    terms.add(term);
    terms.add(secondTerm);
    when(references.getTerms(any(), any())).thenReturn(terms);
    when(term.getName()).thenReturn("ExpandTest");
    when(annotatable.javaAnnotations(any())).thenReturn(Collections.emptyMap());
    when(converter.convert(references, expandAnnotation, annotatable)).thenReturn(Optional.empty());
    when(converter.convert(references, null, annotatable)).thenThrow(NullPointerException.class);

    final Collection<CsdlAnnotation> act = cut.getAnnotations(Applicability.ENTITY_SET, annotatable, references);
    assertTrue(act.isEmpty());
  }

  @Test
  void checkAddReferencesAddCapabilities() throws ODataVocabularyReadException {
    final ReferenceList ref = mock(ReferenceList.class);
    final ReferenceAccess access = mock(ReferenceAccess.class);
    when(ref.addReference(any(), eq("vocabularies/Org.OData.Capabilities.V1.xml"))).thenReturn(access);
    cut.addReferences(ref);
    verify(access).addInclude(any(), eq("Capabilities"));
  }

  @Test
  void checkAddReferencesAddCapabilitiesReThrowsException() throws ODataVocabularyReadException {
    final ReferenceList ref = mock(ReferenceList.class);
    when(ref.addReference(any(), eq("vocabularies/Org.OData.Capabilities.V1.xml")))
        .thenThrow(ODataVocabularyReadException.class);
    assertThrows(ODataVocabularyReadException.class, () -> cut.addReferences(ref));
  }

}
