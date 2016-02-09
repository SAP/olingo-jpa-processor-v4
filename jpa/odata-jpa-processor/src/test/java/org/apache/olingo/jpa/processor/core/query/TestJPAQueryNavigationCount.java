package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Test;

public class TestJPAQueryNavigationCount extends TestBase {

  @Test
  public void testEntitySetCount() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations/$count");
    assertEquals(200, helper.getStatus());

    assertEquals("10", helper.getRawResult());
  }

  @Test
  public void testEntityNavigateCount() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations('3')/Roles/$count");
    assertEquals(200, helper.getStatus());

    assertEquals("3", helper.getRawResult());
  }

}
