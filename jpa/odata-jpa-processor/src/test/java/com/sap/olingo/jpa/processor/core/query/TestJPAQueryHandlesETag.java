package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryHandlesETag extends TestBase {

  static Stream<Arguments> containsETag() {
    return Stream.of(
        arguments("Single entity", "Organizations('3')"),
        arguments("Single entity eTag not selected", "Organizations('3')?$select=ID"),
        arguments("Collection one result", "Organizations?$filter=ID eq '3'"));

  }

  @ParameterizedTest
  @MethodSource("containsETag")
  void testResultContainsETagHeader(final String test, final String url) throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, url);
    helper.assertStatus(200);
    assertNotNull(helper.getHeader(HttpHeader.ETAG), test);
    assertTrue(helper.getHeader(HttpHeader.ETAG).startsWith("\""));
    assertTrue(helper.getHeader(HttpHeader.ETAG).endsWith("\""));
  }

  @Test
  void testResultNotContainsETagHeader() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations");
    helper.assertStatus(200);
    assertNull(helper.getHeader(HttpHeader.ETAG));
  }

  @Test
  void testIfNoneMatchHeaderNotModified() throws IOException, ODataException {
    final Map<String, List<String>> headers = new HashMap<>();
    headers.put(HttpHeader.IF_NONE_MATCH, Arrays.asList("\"0\""));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')", headers);
    helper.assertStatus(304);
    assertNotNull(helper.getHeader(HttpHeader.ETAG), "\"0\"");
    assertTrue(helper.getRawResult().isBlank());
  }

  @Test
  void testIfMatchHeaderPreconditionFailed() throws IOException, ODataException {
    final Map<String, List<String>> headers = new HashMap<>();
    headers.put(HttpHeader.IF_MATCH, Arrays.asList("\"2\""));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')", headers);
    helper.assertStatus(412);
    assertTrue(helper.getRawResult().isBlank());
  }

  @Test
  void testIfMatchHeaderUnknownNotFound() throws IOException, ODataException {
    final Map<String, List<String>> headers = new HashMap<>();
    headers.put(HttpHeader.IF_MATCH, Arrays.asList("\"2\""));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1000')", headers);
    helper.assertStatus(404);
  }
}
