package com.sap.olingo.jpa.processor.core.modify;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ENUMERATION_UNKNOWN;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.PARAMETER_NULL;
import static org.apache.olingo.commons.api.data.ValueType.ENUM;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import com.sap.olingo.jpa.processor.core.query.EdmBindingTargetInfo;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;
import com.sap.olingo.jpa.processor.core.query.Utility;

/**
 * Helper method for modifying requests.
 * <p>
 * Mainly created to increase testability
 * @author Oliver Grande
 *
 */

public class JPAConversionHelper {

  private final Map<Object, Map<String, Object>> getterBuffer;

  public static Object convertParameter(final Parameter param, final JPAServiceDocument sd)
      throws ODataJPAModelException, ODataJPAProcessorException {
    if (param.getValueType() == ENUM) {
      final JPAEnumerationAttribute enumType = sd.getEnumType(param.getType());
      if (enumType == null)
        throw new ODataJPAProcessorException(ENUMERATION_UNKNOWN, BAD_REQUEST, param.getName());
      return enumType.enumOf((Number) param.getValue());
    } else {
      return param.getValue();
    }
  }

  public JPAConversionHelper() {
    super();
    this.getterBuffer = new HashMap<>();
  }

  /**
   * Creates a map of attribute name and the return value of there getter method.
   * <p>
   * It is assumed that the method name is composed from <i>get</> and the
   * name of the attribute and that the attribute name starts with a lower case
   * letter.
   * @param instance
   * @return
   * @throws ODataJPAProcessorException
   */
  public Map<String, Object> buildGetterMap(final Object instance) throws ODataJPAProcessorException {

    if (instance != null) {
      final ODataJPAProcessorException[] exception = { null };
      final Map<String, Object> getterMap = getterBuffer.computeIfAbsent(instance, k -> {
        try {
          return this.determineGetter(instance);
        } catch (final ODataJPAProcessorException e) {
          exception[0] = e;
          return new HashMap<>(1);
        }
      });
      if (exception[0] == null)
        return getterMap;
      else
        throw exception[0];
    } else {
      throw new ODataJPAProcessorException(PARAMETER_NULL, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Converts the payload of a request into the corresponding odata entity
   * @param odata
   * @param request
   * @param requestFormat
   * @param uriResourceParts
   * @return
   * @throws ODataJPAProcessorException
   */
  public Entity convertInputStream(final OData odata, final ODataRequest request, final ContentType requestFormat,
      final List<UriResource> uriResourceParts) throws ODataJPAProcessorException {

    final InputStream requestInputStream = request.getBody();
    final EdmBindingTargetInfo targetEntityInfo = Utility.determineModifyEntitySetAndKeys(uriResourceParts);
    try {
      final ODataDeserializer deserializer = createDeserializer(odata, requestFormat,
          request.getHeaders(HttpHeader.ODATA_VERSION));
      final UriResource lastPart = uriResourceParts.get(uriResourceParts.size() - 1);
      if (lastPart instanceof UriResourceProperty) {
        // Convert requests on property level into request on entity level
        final Entity requestEntity = new Entity();
        final String startProperty = targetEntityInfo.getNavigationPath().split(JPAPath.PATH_SEPARATOR)[0];
        int i = uriResourceParts.size() - 1;
        for (; i > 0; i--) {
          if (uriResourceParts.get(i) instanceof UriResourceProperty
              && ((UriResourceProperty) uriResourceParts.get(i)).getProperty().getName().equals(startProperty)) {
            break;
          }
        }
        List<Property> properties = requestEntity.getProperties();
        for (int j = i; j < uriResourceParts.size() - 1; j++) {
          // NO $value supported yet
          if (!(uriResourceParts.get(i) instanceof UriResourceProperty)) {
            break;
          }
          final EdmProperty edmProperty = ((UriResourceProperty) uriResourceParts.get(i)).getProperty();
          final Property intermediateProperty = new Property();
          intermediateProperty.setType(edmProperty.getType().getFullQualifiedName().getFullQualifiedNameAsString());
          intermediateProperty.setName(edmProperty.getName());
          intermediateProperty.setValue(ValueType.COMPLEX, new ComplexValue());
          properties.add(intermediateProperty);
          properties = ((ComplexValue) intermediateProperty.getValue()).getValue();
        }
        properties.add(deserializer.property(requestInputStream, ((UriResourceProperty) lastPart).getProperty())
            .getProperty());
        return requestEntity;
      } else {
        return deserializer.entity(requestInputStream, targetEntityInfo.getTargetEdmBindingTarget().getEntityType())
            .getEntity();
      }
    } catch (final DeserializerException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  /**
   *
   * @param odata
   * @param request
   * @param edmEntitySet
   * @param et
   * @param newPOJO
   * @return
   * @throws SerializerException
   * @throws ODataJPAProcessorException
   */

  @SuppressWarnings("unchecked")
  public String convertKeyToLocal(final OData odata, final ODataRequest request, final EdmEntitySet edmEntitySet,
      final JPAEntityType et, final Object newPOJO) throws SerializerException, ODataJPAProcessorException {

    if (newPOJO instanceof Map<?, ?>)
      return convertKeyToLocalMap(odata, request, edmEntitySet, et, (Map<String, Object>) newPOJO);
    else
      return convertKeyToLocalEntity(odata, request, edmEntitySet, et, newPOJO);
  }

  /**
   * Creates nested map of attributes and there (new) values. Primitive values are instances of e.g. Integer. Embedded
   * Types are returned as maps.
   *
   * @param odata
   * @param st
   * @param odataProperties
   * @return
   * @throws ODataJPAProcessException
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> convertProperties(final OData odata, final JPAStructuredType st,
      final List<Property> odataProperties) throws ODataJPAProcessException {

    final Map<String, Object> jpaAttributes = new HashMap<>();
    String internalName = null;
    Object jpaAttribute = null;
    JPAPath path;
    for (final Property odataProperty : odataProperties) {
      try {
        path = st.getPath(odataProperty.getName());
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      switch (odataProperty.getValueType()) {
        case COMPLEX:
          try {
            final String name = path.getPath().get(0).getInternalName();
            final JPAStructuredType a = st.getAttribute(name)
                .orElseThrow(() -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND,
                    HttpStatusCode.INTERNAL_SERVER_ERROR, name))
                .getStructuredType();
            internalName = name;
            jpaAttribute = convertProperties(odata, a, ((ComplexValue) odataProperty.getValue()).getValue());
          } catch (final ODataJPAModelException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
          break;
        case PRIMITIVE:
        case ENUM:
          final JPAAttribute attribute = path.getLeaf();
          internalName = attribute.getInternalName();
          jpaAttribute = processAttributeConverter(odataProperty.getValue(), attribute);
          break;
        case COLLECTION_PRIMITIVE:
        case COLLECTION_ENUM:
          final JPAAttribute attribute2 = path.getLeaf();
          internalName = attribute2.getInternalName();
          jpaAttribute = new ArrayList<>();
          for (final Object property : (List<?>) odataProperty.getValue())
            ((List<Object>) jpaAttribute).add(processAttributeConverter(property, attribute2));

          break;
        case COLLECTION_COMPLEX:
          final String name = path.getPath().get(0).getInternalName();
          jpaAttribute = new ArrayList<>();
          try {
            final JPAStructuredType a = st.getAttribute(name)
                .orElseThrow(() -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND,
                    HttpStatusCode.INTERNAL_SERVER_ERROR, name))
                .getStructuredType();
            internalName = name;
            for (final ComplexValue property : (List<ComplexValue>) odataProperty.getValue())
              ((List<Object>) jpaAttribute).add(convertProperties(odata, a, property.getValue()));
          } catch (final ODataJPAModelException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
          break;
        default:
          throw new ODataJPAProcessorException(MessageKeys.NOT_SUPPORTED_PROP_TYPE, HttpStatusCode.NOT_IMPLEMENTED,
              odataProperty.getValueType().name());
      }
      jpaAttributes.put(internalName, jpaAttribute);
    }
    return jpaAttributes;
  }

  /**
   *
   * @param keyPredicates
   * @return
   * @throws ODataJPAFilterException
   * @throws ODataJPAProcessorException
   */
  public Map<String, Object> convertUriKeys(final OData odata, final JPAStructuredType st,
      final List<UriParameter> keyPredicates) throws ODataJPAFilterException, ODataJPAProcessorException {

    final Map<String, Object> result = new HashMap<>(keyPredicates.size());
    String internalName;
    for (final UriParameter key : keyPredicates) {
      try {
        final JPAAttribute attribute = st.getPath(key.getName()).getLeaf();
        internalName = attribute.getInternalName();
        final Object jpaAttribute = ExpressionUtil.convertValueOnAttribute(odata, attribute, key.getText(), true);
        result.put(internalName, jpaAttribute);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
    return result;
  }

  /**
   * Like {@link #buildGetterMap}, but without buffer
   * @param instance
   * @return
   * @throws ODataJPAProcessorException
   */
  public Map<String, Object> determineGetter(final Object instance) throws ODataJPAProcessorException {
    Map<String, Object> getterMap;
    getterMap = new HashMap<>();
    final Method[] methods = instance.getClass().getMethods();
    for (final Method meth : methods) {
      final String methodName = meth.getName();
      if (methodName.substring(0, 3).equals("get") && methodName.length() > 3) {
        final String attributeName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        try {
          final Object value = meth.invoke(instance);
          getterMap.put(attributeName, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new ODataJPAProcessorException(MessageKeys.ATTRIBUTE_RETRIEVAL_FAILED,
              HttpStatusCode.INTERNAL_SERVER_ERROR, e, attributeName);
        }
      }
    }
    return getterMap;
  }

  @SuppressWarnings("unchecked")
  public <S, T> Object processAttributeConverter(final Object value, final JPAAttribute attribute) {
    Object jpaAttribute;
    if (attribute.getConverter() != null) {
      final AttributeConverter<T, S> converter = attribute.getConverter();
      jpaAttribute = converter.convertToEntityAttribute((S) value);
    } else if (attribute.isEnum()) {
      jpaAttribute = findEnumConstantsByOrdinal(attribute.getType().getEnumConstants(), value);
    } else {
      jpaAttribute = value;
    }
    return jpaAttribute;
  }

  private void collectKeyProperties(final Map<String, Object> newPOJO, final List<JPAPath> keyPath,
      final List<Property> properties)
      throws ODataJPAProcessorException, ODataJPAModelException {

    if (keyPath.size() > 1) {

      for (final JPAPath key : keyPath) {
        final Object keyElement = newPOJO.get(key.getLeaf().getInternalName());
        final Property property = new Property(null, key.getLeaf().getExternalName());
        property.setValue(ValueType.PRIMITIVE, keyElement);
        properties.add(property);
      }
    } else {
      final JPAPath key = keyPath.get(0);
      if (key.getLeaf().isComplex()) {
        // EmbeddedId
        @SuppressWarnings("unchecked")
        final Map<String, Object> embeddedId = (Map<String, Object>) newPOJO.get(key.getLeaf().getInternalName());
        collectKeyProperties(embeddedId, key.getLeaf().getStructuredType().getPathList(), properties);
      } else {
        final Property property = new Property(null, key.getLeaf().getExternalName());
        property.setValue(ValueType.PRIMITIVE, newPOJO.get(key.getLeaf().getInternalName()));
        properties.add(property);
      }
    }
  }

  private void collectKeyProperties(final Object newPOJO, final List<JPAPath> keyPath, final List<Property> properties)
      throws ODataJPAProcessorException, ODataJPAModelException {

    final Map<String, Object> getter = buildGetterMap(newPOJO);
    if (keyPath.size() > 1) {

      for (final JPAPath key : keyPath) {
        final Property property = new Property(null, key.getLeaf().getExternalName());
        property.setValue(ValueType.PRIMITIVE, getter.get(key.getLeaf().getInternalName()));
        properties.add(property);
      }
    } else {
      final JPAPath key = keyPath.get(0);
      if (key.getLeaf().isComplex()) {
        // EmbeddedId
        final Object embeddedId = getter.get(key.getLeaf().getInternalName());
        collectKeyProperties(embeddedId, key.getLeaf().getStructuredType().getPathList(), properties);
      } else {
        final Property property = new Property(null, key.getLeaf().getExternalName());
        property.setValue(ValueType.PRIMITIVE, getter.get(key.getLeaf().getInternalName()));
        properties.add(property);
      }
    }
  }

  private String convertKeyToLocalEntity(final OData odata, final ODataRequest request, final EdmEntitySet edmEntitySet,
      final JPAEntityType et, final Object newPOJO) throws SerializerException, ODataJPAProcessorException {

    final Entity createdEntity = new Entity();

    try {
      final List<JPAPath> keyPath = et.getKeyPath();
      final List<Property> properties = createdEntity.getProperties();

      collectKeyProperties(newPOJO, keyPath, properties);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    return request.getRawBaseUri() + '/'
        + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
  }

  private String convertKeyToLocalMap(final OData odata, final ODataRequest request,
      final EdmEntitySet edmEntitySet, final JPAEntityType et, final Map<String, Object> newPOJO)
      throws SerializerException, ODataJPAProcessorException {

    final Entity createdEntity = new Entity();

    try {
      final List<Property> properties = createdEntity.getProperties();
      final List<JPAPath> keyPath = et.getKeyPath();
      collectKeyProperties(newPOJO, keyPath, properties);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    return request.getRawBaseUri() + '/'
        + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
  }

  private ODataDeserializer createDeserializer(final OData odata, final ContentType requestFormat,
      final List<String> version) throws DeserializerException {
    return odata.createDeserializer(requestFormat, version);
  }

  private <T> Object findEnumConstantsByOrdinal(final T[] enumConstants, final Object value) {
    for (int i = 0; i < enumConstants.length; i++) {
      if (((Enum<?>) enumConstants[i]).ordinal() == (Integer) value)
        return enumConstants[i];
    }
    return null;
  }

}
