package com.sap.olingo.jpa.processor.core.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPAEntityResultConverterTest extends TestBase {

  TestHelper helper;
  JPAEntityResultConverter cut;

  @BeforeEach
  void setup() throws ODataException {
    helper = getHelper();
  }

  @Test
  void testEtagAdded() throws ODataJPAModelException, SerializerException, ODataApplicationException,
      URISyntaxException {

    final var personEntityType = helper.getJPAEntityType(Person.class);
    final var personEdmType = mock(EdmEntityType.class);
    when(personEdmType.getNamespace()).thenReturn(personEntityType.getExternalFQN().getNamespace());
    when(personEdmType.getName()).thenReturn(personEntityType.getExternalFQN().getName());
    final var result = new Person();
    result.setID("123");
    result.setETag(12);
    final List<Person> results = Arrays.asList(result);

    cut = new JPAEntityResultConverter(OData.newInstance().createUriHelper(), helper.sd, results, personEdmType);
    final var act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("12", act.getEntities().get(0).getETag());
  }
}
