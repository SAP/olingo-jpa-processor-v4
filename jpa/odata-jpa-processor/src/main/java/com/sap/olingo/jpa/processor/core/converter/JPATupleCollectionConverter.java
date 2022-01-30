package com.sap.olingo.jpa.processor.core.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPATupleCollectionConverter extends JPATupleResultConverter {

  public JPATupleCollectionConverter(final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata, final JPAODataRequestContextAccess requestContext) {
    super(sd, uriHelper, serviceMetadata, requestContext);
  }

  @Override
  public Map<String, List<Object>> getResult(final JPAExpandResult dbResult,
      final Collection<JPAPath> requestedSelection) throws ODataApplicationException {

    jpaQueryResult = dbResult;
    final JPACollectionResult jpaResult = (JPACollectionResult) dbResult;
    final JPAAssociationAttribute attribute = jpaResult.getAssociation().getLeaf();
    final boolean isTransient = attribute.isTransient();

    final Map<String, List<Tuple>> childResult = jpaResult.getResults();
    final Map<String, List<Object>> result = new HashMap<>(childResult.size());

    try {
      final JPAStructuredType st = determineCollectionRoot(jpaResult.getEntityType(), jpaResult.getAssociation()
          .getPath());
      final String prefix = determinePrefix(jpaResult.getAssociation().getAlias());

      for (Entry<String, List<Tuple>> tuple : childResult.entrySet()) {
        if (isTransient) {
          result.put(tuple.getKey(), convertTransientCollection(attribute, tuple));
        } else {
          result.put(tuple.getKey(),
              convertPersistentCollection(jpaResult, attribute, st, prefix, tuple, requestedSelection));
        }
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } finally {
      childResult.replaceAll((k, v) -> null);
    }
    return result;
  }

  private List<Object> convertPersistentCollection(final JPACollectionResult jpaResult,
      final JPAAssociationAttribute attribute, final JPAStructuredType st, final String prefix,
      Entry<String, List<Tuple>> tuple, final Collection<JPAPath> requestedSelection) throws ODataJPAModelException,
      ODataApplicationException {

    final List<Object> collection = new ArrayList<>();
    final List<Tuple> rows = tuple.getValue();

    for (final Tuple row : rows) {
      if (attribute.isComplex()) {
        final ComplexValue value = new ComplexValue();
        final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
        if (requestedSelection.isEmpty()) {
          convertWithOutSelection(st, prefix, row, value, complexValueBuffer);
        } else {
          convertRowWithSelection(row, requestedSelection, complexValueBuffer, null, value.getValue());
        }
        collection.add(complexValueBuffer.get(jpaResult.getAssociation().getAlias()));
      } else {
        collection.add(convertPrimitiveCollectionAttribute(row.get(jpaResult.getAssociation().getAlias()),
            (JPACollectionAttribute) attribute));
      }

    }
    return collection;
  }

  private void convertWithOutSelection(final JPAStructuredType st, final String prefix, final Tuple row,
      final ComplexValue value, final Map<String, ComplexValue> complexValueBuffer) throws ODataJPAModelException,
      ODataApplicationException {

    for (final TupleElement<?> element : row.getElements()) {
      final String alias = determineAlias(element.getAlias(), prefix);
      if (alias != null) {
        final JPAPath path = st.getPath(alias);
        convertAttribute(row.get(element.getAlias()), path, complexValueBuffer,
            value.getValue(), row, prefix, null);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object> convertTransientCollection(final JPAAssociationAttribute attribute,
      Entry<String, List<Tuple>> tuple) throws ODataJPAProcessorException {

    final Optional<EdmTransientPropertyCalculator<?>> calculator = requestContext.getCalculator(attribute);
    if (calculator.isPresent()) {
      // The tuple contains only one row with required fields
      return (List<Object>) calculator.get().calculateCollectionProperty(tuple.getValue().get(0));
    }
    return Collections.emptyList();
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
