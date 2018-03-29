package com.sap.olingo.jpa.processor.core.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AttributeConverter;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPATupleCollectionConverter extends JPATupleResultConverter {

  public JPATupleCollectionConverter(JPAServiceDocument sd, UriHelper uriHelper, ServiceMetadata serviceMetadata) {
    super(sd, uriHelper, serviceMetadata);
  }

  @Override
  public Map<String, List<Object>> getResult(final JPAExpandResult dbResult) throws ODataApplicationException {

    jpaQueryResult = dbResult;
    final JPACollectionResult jpaResult = (JPACollectionResult) dbResult;
    final JPAAssociationAttribute attribute = jpaResult.getAssoziation().getLeaf();
    final boolean isComplex = attribute.isComplex();

    final Map<String, List<Tuple>> childResult = jpaResult.getResults();
    final Map<String, List<Object>> result = new HashMap<>(childResult.size());
    try {
      final JPAStructuredType st = determineCollectionRoot(jpaResult.getEntityType(), jpaResult.getAssoziation()
          .getPath());
      final String prefix = determinePrefix(jpaResult.getAssoziation().getAlias());

      for (Entry<String, List<Tuple>> tuple : childResult.entrySet()) {
        final List<Object> collection = new ArrayList<>();
        final List<Tuple> rows = tuple.getValue();
        for (int i = 0; i < rows.size(); i++) {
          final Tuple row = rows.set(i, null);
          if (isComplex) {
            final ComplexValue value = new ComplexValue();
            final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
            complexValueBuffer.put(jpaResult.getAssoziation().getAlias(), value);
            for (final TupleElement<?> element : row.getElements()) {
              final JPAPath path = st.getPath(determineAlias(element.getAlias(), prefix));
              convertAttribute(row.get(element.getAlias()), path, complexValueBuffer,
                  value.getValue(), row, prefix, "");

            }
            collection.add(value);
          } else {
            collection.add(convertPrimitiveCollectionAttribute(row.get(jpaResult.getAssoziation().getAlias()),
                (JPACollectionAttribute) attribute));
          }
        }
        result.put(tuple.getKey(), collection);
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } finally {
      childResult.replaceAll((k, v) -> null);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T extends Object, S extends Object> S convertPrimitiveCollectionAttribute(final Object value,
      final JPAAttribute attribute) {

    if (attribute.getConverter() != null) {
      final AttributeConverter<T, S> converter = attribute.getConverter();
      return converter.convertToDatabaseColumn((T) value);
    }
    return (S) value;
  }
}
