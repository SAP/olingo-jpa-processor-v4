package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCapabilitiesAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryNavigationCount extends TestBase {

  static Stream<Arguments> provideCountQueries() {
    return Stream.of(
        arguments("EntitySetCount", "Organizations/$count", "10"),
        arguments("EntityNavigateCount", "Organizations('3')/Roles/$count", "3"),
        arguments("EntitySetCountWithFilterOn", "Organizations/$count?$filter=Address/HouseNumber gt '30'", "7"),
        arguments("EntitySetCountWithFilterOnDescription", "Persons/$count?$filter=LocationName eq 'Deutschland'",
            "2"));
  }

  @ParameterizedTest
  @MethodSource("provideCountQueries")
  void testEntitySetCount(final String text, final String queryString, final String numberOfResults)
      throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString);
    assertEquals(200, helper.getStatus());

    assertEquals(numberOfResults, helper.getRawResult(), text);
  }

  @Test
  void testCountNotSupported() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents(CodePublisher='Eurostat',CodeID='NUTS2',DivisionCode='BE24')/Children/$count",
        new JavaBasedCapabilitiesAnnotationsProvider());

    assertEquals(400, helper.getStatus());
  }
}
