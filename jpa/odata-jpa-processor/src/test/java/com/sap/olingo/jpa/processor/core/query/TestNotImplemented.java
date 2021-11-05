package com.sap.olingo.jpa.processor.core.query;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestNotImplemented extends TestBase {

  @Test
  void testApplyThrowsException() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$apply=aggregate(Area with sum as TotalArea)");
    helper.assertStatus(501);
  }

}
