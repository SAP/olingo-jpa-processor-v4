package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAApiVersionProviderTest {

  private JPAApiVersionProvider.Builder builder;
  private EntityManagerFactory emf;
  private JPAEdmMetadataPostProcessor postProcessor;

  @BeforeEach
  void setup() {
    emf = mock(EntityManagerFactory.class);
    postProcessor = mock(JPAEdmMetadataPostProcessor.class);
    builder = JPAApiVersionProvider.with();
  }

  @Test
  void testBuilderSetIdThrowsNullPointerExceptionOnNull() {
    assertThrows(NullPointerException.class, () -> builder.setId(null));
  }

  @Test
  void testGetIdReturnsSetValue() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf).build();
    assertEquals("V1", cut.getId());
  }

  @Test
  void testBuilderThrowsExceptionOnMissingId() {
    final var act = assertThrows(ODataJPAModelException.class, () -> builder.setEntityManagerFactory(emf).build());
    assertEquals(ODataJPAModelException.MessageKeys.VERSION_ID_MISSING.getKey(), act.getId());
  }

  @Test
  void testBuilderThrowsExceptionOnEmptyId() {
    final var act = assertThrows(ODataJPAModelException.class, () -> builder.setId("")
        .setEntityManagerFactory(emf)
        .build());
    assertEquals(ODataJPAModelException.MessageKeys.VERSION_ID_MISSING.getKey(), act.getId());
  }

  @Test
  void testBuilderSetEntityManagerFactoryThrowsNullPointerExceptionOnNull() {
    assertThrows(NullPointerException.class, () -> builder.setEntityManagerFactory(null));
  }

  @Test
  void testBuilderThrowsExceptionOnMissingEntityManagerFactory() {
    assertThrows(ODataJPAModelException.class, () -> builder.setId("V1").build());
  }

  @Test
  void testGetEntityManagerFactoryReturnsSetValue() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf).build();
    assertEquals(emf, cut.getEntityManagerFactory());
  }

  @Test
  void testGetTypePackageReturnsSetValue() throws ODataJPAModelException {
    final var packageNames = new String[] { "com.test.action", "com.test.enum" };
    final var cut = builder.setId("V1").setEntityManagerFactory(emf)
        .setTypePackage(packageNames)
        .build();
    assertEquals(packageNames, cut.getPackageNames());
  }

  @Test
  void testGetTypePackageReturnsEmptyArrayIfNotProvided() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf).build();
    assertEquals(0, cut.getPackageNames().length);
  }

  @Test
  void testGetRequestMappingPathReturnsSetValue() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf)
        .setRequestMappingPath("/test/v1")
        .build();
    assertEquals("/test/v1", cut.getRequestMappingPath());
  }

  @Test
  void testGetMetadataPostProcessorReturnsSetValue() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf)
        .setMetadataPostProcessor(postProcessor)
        .build();
    assertEquals(postProcessor, cut.getMetadataPostProcessor());
  }

  @Test
  void testGetMetadataPostProcessorReturnsDefaultIfNotProvided() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf).build();
    assertNotNull(cut.getMetadataPostProcessor());
  }

  @Test
  void testHideRestrictedPropertiesReturnsDefaultFalse() throws ODataJPAModelException {
    final var cut = builder.setId("V1").setEntityManagerFactory(emf).build();
    assertFalse(cut.hideRestrictedProperties());
  }

  @Test
  void testHideRestrictedPropertiesReturnsSetValue() throws ODataJPAModelException {
    final var cut = builder.setId("V1")
        .setEntityManagerFactory(emf)
        .setHideRestrictedProperties(true)
        .build();
    assertTrue(cut.hideRestrictedProperties());
  }
}
