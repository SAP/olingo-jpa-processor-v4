package com.sap.olingo.jpa.processor.core.converter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public class JPAComplexResultConverter extends JPAStructuredResultConverter {

  public JPAComplexResultConverter(final JPAServiceDocument sd, final List<?> jpaQueryResult,
      final EdmComplexType edmComplexType) {

    super(jpaQueryResult, sd.getComplexType(edmComplexType));
  }

  @Override
  public List<ComplexValue> getResult() throws ODataApplicationException, SerializerException, URISyntaxException {
    List<ComplexValue> result = new ArrayList<>();

    for (Object row : this.jpaQueryResult) {
      final ComplexValue value = new ComplexValue();
      final List<Property> properties = value.getValue();
      convertProperties(row, properties, jpaTopLevelType);
      result.add(value);
    }
    return result;
  }

}
