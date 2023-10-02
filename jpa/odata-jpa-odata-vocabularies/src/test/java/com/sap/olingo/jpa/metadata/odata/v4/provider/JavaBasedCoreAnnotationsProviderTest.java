package com.sap.olingo.jpa.metadata.odata.v4.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaBasedCoreAnnotationsProviderTest {

  private JavaBasedCoreAnnotationsProvider cut;

  @BeforeEach
  void setup() {
    cut = new JavaBasedCoreAnnotationsProvider();
  }

  @Test
  void testGetAlias() {
    assertEquals("Core", cut.getAlias());
  }

  @Test
  void testGetNameSpace() {
    assertFalse(cut.getNameSpace().isEmpty());
  }

  @Test
  void testGetPath() {
    assertFalse(cut.getPath().isEmpty());
  }

  @Test
  void testGetUri() throws URISyntaxException {
    assertNotNull(cut.getUri());
  }

  @Test
  void checkCreateWithConverter() {
    final JavaAnnotationConverter converter = mock(JavaAnnotationConverter.class);
    cut = new JavaBasedCoreAnnotationsProvider(converter);
    assertEquals(converter, cut.converter);
    assertNotNull(cut.packageName);
  }
}
