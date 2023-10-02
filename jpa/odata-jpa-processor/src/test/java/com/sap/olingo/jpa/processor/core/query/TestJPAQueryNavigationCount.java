package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCapabilitiesAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryNavigationCount extends TestBase {

  @Test
  void testEntitySetCount() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations/$count");
    assertEquals(200, helper.getStatus());

    assertEquals("10", helper.getRawResult());
  }

  @Test
  void testEntityNavigateCount() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Roles/$count");
    assertEquals(200, helper.getStatus());

    assertEquals("3", helper.getRawResult());
  }

  @Test
  void testEntitySetCountWithFilterOn() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations/$count?$filter=Address/HouseNumber gt '30'");

    assertEquals(200, helper.getStatus());
    assertEquals("7", helper.getRawResult());
  }

  @Test
  void testEntitySetCountWithFilterOnDescription() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons/$count?$filter=LocationName eq 'Deutschland'",
        new JavaBasedCapabilitiesAnnotationsProvider());

    assertEquals(200, helper.getStatus());
    assertEquals("2", helper.getRawResult());
  }

  @Test
  void testCountNotSupported() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents(CodePublisher='Eurostat',CodeID='NUTS2',DivisionCode='BE24')/Children/$count",
        new JavaBasedCapabilitiesAnnotationsProvider());

    assertEquals(400, helper.getStatus());
  }
}
