package com.sap.olingo.jpa.processor.core.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPATupleAbstractConverter {

  public static final String      ACCESS_MODIFIER_GET = "get";
  public static final String      ACCESS_MODIFIER_SET = "set";
  public static final String      ACCESS_MODIFIER_IS  = "is";
  protected final JPAEntityType   jpaConversionTargetEntity;
  protected final JPAExpandResult jpaQueryResult;
  protected final UriHelper       uriHelper;
  protected final String          setName;
  protected final JPAServiceDocument sd;
  protected final ServiceMetadata serviceMetadata;
  protected final EdmEntityType   edmType;

  public JPATupleAbstractConverter(final JPAExpandResult jpaQueryResult,
      final UriHelper uriHelper, final JPAServiceDocument sd, final ServiceMetadata serviceMetadata)
      throws ODataApplicationException {
    super();

    this.jpaConversionTargetEntity = jpaQueryResult.getEntityType();
    this.jpaQueryResult = jpaQueryResult;
    this.uriHelper = uriHelper;
    this.sd = sd;
    this.serviceMetadata = serviceMetadata;
    this.setName = determineSetName(jpaQueryResult, sd);
    this.edmType = determineEdmType();
  }

  protected String buildConcatenatedKey(final Tuple row, final List<JPAOnConditionItem> joinColumns) {
    final StringBuffer buffer = new StringBuffer();
    for (final JPAOnConditionItem item : joinColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      // TODO Tuple returns the converted value in case a @Convert(converter = annotation is given
      buffer.append(row.get(item.getLeftPath().getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  protected Entity convertRow(final JPAEntityType rowEntity, final Tuple row) throws ODataApplicationException {
    final Map<String, ComplexValue> complexValueBuffer = new HashMap<String, ComplexValue>();
    final Entity odataEntity = new Entity();

    // odataEntity.setType(rowEntity.getExternalFQN().getFullQualifiedNameAsString());
    odataEntity.setType(edmType.getFullQualifiedName().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();
    // TODO store @Version to fill ETag Header
    for (final TupleElement<?> element : row.getElements()) {
      try {
        convertAttribute(row.get(element.getAlias()), element.getAlias(), "", rowEntity, complexValueBuffer,
            properties);
      } catch (ODataJPAModelException e) {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }
    try {
      odataEntity.setId(createId(jpaConversionTargetEntity.getKey(), odataEntity));
    } catch (ODataJPAModelException e) {

      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_KEY_PROPERTY_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, jpaConversionTargetEntity.getExternalName());
    }
    for (final String attribute : complexValueBuffer.keySet()) {
      final ComplexValue complexValue = complexValueBuffer.get(attribute);
      complexValue.getNavigationLinks().addAll(createExpand(row, odataEntity.getId())); // , attribute));
    }
    odataEntity.getNavigationLinks().addAll(createExpand(row, odataEntity.getId()));
    return odataEntity;
  }

  protected Collection<? extends Link> createExpand(final Tuple row, final URI uri) // , final String attributeName)
      throws ODataApplicationException {
    final List<Link> entityExpandLinks = new ArrayList<Link>();
    // jpaConversionTargetEntity.
    final Map<JPAAssociationPath, JPAExpandResult> children = jpaQueryResult.getChildren();
    if (children != null) {
      for (final JPAAssociationPath associationPath : children.keySet()) {
        try {
          if (jpaConversionTargetEntity.getDeclaredAssociation(associationPath) != null) {
            final Link expand = new JPATupleExpandResultConverter(children.get(associationPath), row,
                associationPath, uriHelper, sd, serviceMetadata).getResult();
            // TODO Check how to convert Organizations('3')/AdministrativeInformation?$expand=Created/User
            entityExpandLinks.add(expand);
          }
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_ERROR,
              HttpStatusCode.INTERNAL_SERVER_ERROR, associationPath.getAlias());
        }
      }
    }
    return entityExpandLinks;
  }

  protected URI createId(final List<? extends JPAAttribute> keyAttributes, final Entity entity)
      throws ODataApplicationException, ODataRuntimeException {

    try {
      // TODO Clarify host-name and port as part of ID see
      // http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792702

      final StringBuffer uriString = new StringBuffer(setName);
      uriString.append("(");
      uriString.append(uriHelper.buildKeyPredicate(edmType, entity));
      uriString.append(")");
      return new URI(uriString.toString());
    } catch (URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmType.getName(), e);
    } catch (IllegalArgumentException e) {
      return null;
    } catch (SerializerException e) {
      throw new ODataRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Object, S extends Object> void convertAttribute(final Object value, final String externalName,
      final String prefix, final JPAStructuredType jpaStructuredType,
      final Map<String, ComplexValue> complexValueBuffer,
      final List<Property> properties) throws ODataJPAModelException {

    ComplexValue compexValue = null;
    if (jpaStructuredType.getPath(externalName) != null) {
      final JPAAttribute attribute = (JPAAttribute) jpaStructuredType.getPath(externalName).getPath().get(0);// getLeaf();
      if (attribute != null && !attribute.isKey() && attribute.isComplex()) {
        String bufferKey;
        if (prefix.isEmpty())
          bufferKey = attribute.getExternalName();
        else
          bufferKey = prefix + JPAPath.PATH_SEPERATOR + attribute.getExternalName();
        compexValue = complexValueBuffer.get(bufferKey);
        if (compexValue == null) {
          compexValue = new ComplexValue();
          complexValueBuffer.put(bufferKey, compexValue);
          properties.add(new Property(
              attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
              attribute.getExternalName(),
              ValueType.COMPLEX,
              compexValue));
        }
        final List<Property> values = compexValue.getValue();
        final int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPERATOR.length();
        final String attributeName = externalName.substring(splitIndex);
        convertAttribute(value, attributeName, bufferKey, attribute.getStructuredType(), complexValueBuffer, values);
      } else {
        Object odataValue;
        if (attribute.getConverter() != null) {
          AttributeConverter<T, S> converter = (AttributeConverter<T, S>) attribute.getConverter();
          odataValue = converter.convertToDatabaseColumn((T) value);
        } else
          odataValue = value;
        if (attribute != null && attribute.isKey() && attribute.isComplex()) {

          properties.add(new Property(
              null,
              jpaStructuredType.getPath(externalName).getLeaf().getExternalName(),
              ValueType.PRIMITIVE,
              odataValue));
        } else {
          // ...$select=Name1,Address/Region
          properties.add(new Property(
              null,
              externalName,
              ValueType.PRIMITIVE,
              odataValue));
        }
      }
    }
  }

  private EdmEntityType determineEdmType() {
    try {
      final JPAEntitySet es = sd.getEntitySet(jpaQueryResult.getEntityType());
      return serviceMetadata.getEdm().getEntityType(es.getODataEntityType().getExternalFQN());
    } catch (ODataJPAModelException e) {
      throw new ODataRuntimeException(e);
    }
  }

//  protected Map<String, Method> getGetter(final JPAAttribute structuredAttribute) {
//    HashMap<String, Method> pojoMethods = methodsBuffer.get(structuredAttribute.getInternalName());
//    if (pojoMethods == null) {
//      pojoMethods = new HashMap<String, Method>();
//      final Method[] allMethods = structuredAttribute.getStructuredType().getTypeClass().getMethods();
//      for (final Method m : allMethods) {
//        pojoMethods.put(m.getName(), m);
//      }
//      methodsBuffer.put(structuredAttribute.getInternalName(), pojoMethods);
//    }
//    return pojoMethods;
//  }

  private String determineSetName(final JPAExpandResult jpaQueryResult, final JPAServiceDocument sd)
      throws ODataJPAQueryException {
    try {
      return sd.getEntitySet(jpaQueryResult.getEntityType()).getExternalName();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_SET_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, jpaQueryResult.getEntityType().getExternalFQN()
              .getFullQualifiedNameAsString());
    }
  }

}