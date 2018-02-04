package com.sap.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

public class TestJPAQueryCollection extends TestBase {

  @Test
  public void testSelectPropertyAndCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$select=ID,Comment&orderby=ID");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    ObjectNode org = (ObjectNode) orgs.get(0);
    assertNotNull(org.get("ID"));
    ArrayNode comment = (ArrayNode) org.get("Comment");
    assertEquals(2, comment.size());
  }

  @Ignore // See https://issues.apache.org/jira/browse/OLINGO-1231
  @Test
  public void testSelectPropertyOfCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/InhouseAddress?$select=Building");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    ObjectNode org = (ObjectNode) orgs.get(0);
    ArrayNode comment = (ArrayNode) org.get("Comment");
    assertEquals(2, comment.size());
  }

  @Test
  public void testSelectAllWithComplexCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')?$select=*");
    helper.assertStatus(200);

    final ObjectNode person = helper.getValue();
    ArrayNode comment = (ArrayNode) person.get("InhouseAddress");
    assertEquals(2, comment.size());
  }

  @Test
  public void testSelectAllWithPrimitiveCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$select=*");
    helper.assertStatus(200);

    final ObjectNode person = helper.getValue();
    ArrayNode comment = (ArrayNode) person.get("Comment");
    assertEquals(2, comment.size());
  }
}
