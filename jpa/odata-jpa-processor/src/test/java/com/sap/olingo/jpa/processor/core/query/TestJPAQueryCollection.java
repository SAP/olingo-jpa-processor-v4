package com.sap.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

public class TestJPAQueryCollection extends TestBase {
  @Test
  public void testSelectPropertyAndCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$select=ID,Comment");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    ObjectNode org = (ObjectNode) orgs.get(0);
    ArrayNode comment = (ArrayNode) org.get("Comment");
    assertEquals(2, comment.size());
  }
}
