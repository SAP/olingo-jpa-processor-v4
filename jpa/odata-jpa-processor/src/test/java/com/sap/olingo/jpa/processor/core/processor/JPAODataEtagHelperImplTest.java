package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.etag.PreconditionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEtagValidator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAODataEtagHelperImplTest {
  // See org.apache.olingo.server.core.etag.ETagParser
  private static final Pattern ETAG = Pattern.compile("\\s*(,\\s*)+|((?:W/)?\"[!#-~\\x80-\\xFF]*\")");

  private JPAODataEtagHelperImpl cut;
  private OData odata;
  private ETagHelper olingoHelper;

  @BeforeEach
  void setup() {
    odata = mock(OData.class);
    olingoHelper = mock(ETagHelper.class);
    when(odata.createETagHelper()).thenReturn(olingoHelper);
    cut = new JPAODataEtagHelperImpl(odata);
  }

  @Test
  void testCheckReadPreconditionsCallsOlingo() throws PreconditionException {
    final String etag = "";
    final Collection<String> ifMatchHeaders = Collections.emptyList();
    final Collection<String> ifNoneMatchHeaders = Collections.emptyList();
    cut.checkReadPreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
    verify(olingoHelper, times(1)).checkReadPreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
  }

  @Test
  void testCheckChangePreconditionsCallsOlingo() throws PreconditionException {
    final String etag = "";
    final Collection<String> ifMatchHeaders = Collections.emptyList();
    final Collection<String> ifNoneMatchHeaders = Collections.emptyList();
    cut.checkChangePreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
    verify(olingoHelper, times(1)).checkChangePreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
  }

  @Test
  void testAsEtagStringStrong() throws ODataJPAModelException, ODataJPAQueryException {
    final JPAEntityType et = createEntityType("Person", true, JPAEtagValidator.STRONG);
    final var act = cut.asEtag(et, 12L);
    assertEquals("\"12\"", act);
    assertEtagConsistent(act);
  }

  @Test
  void testAsEtagStringWeak() throws ODataJPAModelException, ODataJPAQueryException {
    final JPAEntityType et = createEntityType("Person", true, JPAEtagValidator.WEAK);
    final var act = cut.asEtag(et, 12L);
    assertEquals("W/\"12\"", act);
    assertEtagConsistent(act);
  }

  @Test
  void testAsEtagStringTimestamp() throws ODataJPAModelException, ODataJPAQueryException {
    final JPAEntityType et = createEntityType("Person", true, JPAEtagValidator.WEAK);
    final Instant i = Instant.now();
    final var act = cut.asEtag(et, Timestamp.from(i));
    assertEquals("W/\"" + i.toString() + "\"", act);
    assertEtagConsistent(act);
  }

  @Test
  void testAsEtagStringValueNullEmptyString() throws ODataJPAModelException, ODataJPAQueryException {
    final JPAEntityType et = createEntityType("Person", true, JPAEtagValidator.WEAK);
    final var act = cut.asEtag(et, null);
    assertEquals("", act);
  }

  @Test
  void testAsEtagStringEntityTypeNoEtag() throws ODataJPAModelException, ODataJPAQueryException {
    final JPAEntityType et = createEntityType("Person", false, JPAEtagValidator.STRONG);
    final var act = cut.asEtag(et, null);
    assertNull(act);
  }

  private JPAEntityType createEntityType(final String name, final boolean hasEtag, final JPAEtagValidator validator)
      throws ODataJPAModelException {
    final var et = mock(JPAEntityType.class);
    when(et.getExternalName()).thenReturn(name);
    when(et.hasEtag()).thenReturn(hasEtag);
    when(et.getEtagValidator()).thenReturn(validator);
    return et;
  }

  private void assertEtagConsistent(final String value) {

    final Matcher matcher = ETAG.matcher(value.trim());
    assertTrue(matcher.matches(), "Match: " + matcher.find());

  }
}
