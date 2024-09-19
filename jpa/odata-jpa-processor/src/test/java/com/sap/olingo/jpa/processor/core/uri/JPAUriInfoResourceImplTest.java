package com.sap.olingo.jpa.processor.core.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAUriInfoResourceImplTest {
  private JPAUriInfoResourceImpl cut;
  private UriInfoResource original;
  private CustomQueryOption customQueryOption;
  private FormatOption formatOption;
  private IdOption idOption;
  private DeltaTokenOption deltaTokenOption;

  @BeforeEach
  void setup() {
    original = mock(UriInfoResource.class);
    customQueryOption = mock(CustomQueryOption.class);
    formatOption = mock(FormatOption.class);
    idOption = mock(IdOption.class);
    deltaTokenOption = mock(DeltaTokenOption.class);
    when(original.getCustomQueryOptions()).thenReturn(Collections.singletonList(customQueryOption));
    when(original.getFormatOption()).thenReturn(formatOption);
    when(original.getIdOption()).thenReturn(idOption);
    when(original.getDeltaTokenOption()).thenReturn(deltaTokenOption);
    when(original.getValueForAlias("@a")).thenReturn("Test");
    cut = new JPAUriInfoResourceImpl(original);
  }

  @Test
  void testGetCustomQueryOptions() {
    assertEquals(customQueryOption, cut.getCustomQueryOptions().get(0));
  }

  @Test
  void testGetValueForAlias() {
    assertEquals("Test", cut.getValueForAlias("@a"));
  }

  @Test
  void testGetFormatOption() {
    assertEquals(formatOption, cut.getFormatOption());
  }

  @Test
  void testGetIdOption() {
    assertEquals(idOption, cut.getIdOption());
  }

  @Test
  void testGetDeltaTokenOption() {
    assertEquals(deltaTokenOption, cut.getDeltaTokenOption());
  }

}
