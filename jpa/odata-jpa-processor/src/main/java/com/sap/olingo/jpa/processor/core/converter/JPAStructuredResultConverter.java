package com.sap.olingo.jpa.processor.core.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Abstract super class to converts a list of JPA POJOs into Olingo format. The POJOs have to have
 * @author Oliver Grande
 *
 */

public abstract class JPAStructuredResultConverter {

  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";
  private static final Map<String, HashMap<String, Method>> METHOD_BUFFER =
      new HashMap<>();
  protected final List<?> jpaQueryResult;
  protected final JPAStructuredType jpaTopLevelType;

  public JPAStructuredResultConverter(final List<?> jpaQueryResult, final JPAStructuredType jpaStructuredType) {

    super();
    this.jpaQueryResult = jpaQueryResult;
    this.jpaTopLevelType = jpaStructuredType;
  }

  public abstract Object getResult() throws ODataApplicationException, SerializerException, URISyntaxException;

  protected Map<String, Method> getMethods(final Class<?> clazz) {
    HashMap<String, Method> methods = METHOD_BUFFER.get(clazz.getName());
    if (methods == null) {
      methods = new HashMap<>();

      final Method[] allMethods = clazz.getMethods();
      for (final Method m : allMethods) {
        if (m.getReturnType().getName() != "void"
            && Modifier.isPublic(m.getModifiers()))
          methods.put(m.getName(), m);
      }
      METHOD_BUFFER.put(clazz.getName(), methods);
    }
    return methods;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Object, S extends Object> void convertProperties(final Object row,
      final List<Property> properties, final JPAStructuredType jpaStructuredType) throws ODataJPAQueryException {

    List<JPAAttribute> attributeList;
    final Map<String, Method> methodMap = getMethods(jpaStructuredType.getTypeClass());
    try {
      attributeList = jpaStructuredType.getAttributes();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    for (final JPAAttribute attribute : attributeList) {
      final String attributeName = attribute.getInternalName();
      final Method getMethod = getGetter(attributeName, methodMap);
      try {
        if (attribute.isCollection()) {
          final Collection<?> odataValue = (Collection<?>) getMethod.invoke(row);
          final List<Object> collection = new ArrayList<>();
          if (attribute.isComplex() && odataValue != null) {
            for (final Object element : odataValue) {
              final ComplexValue values = new ComplexValue();
              convertProperties(element, values.getValue(), attribute.getStructuredType());
              collection.add(values);
            }
          } else if (odataValue != null)
            collection.addAll(odataValue);

          properties.add(new Property(
              attribute.getExternalFQN().getFullQualifiedNameAsString(),
              attribute.getExternalName(),
              attribute.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
              collection));

        } else if (attribute.isComplex()) {
          final ComplexValue complexValue = new ComplexValue();
          properties.add(new Property(
              attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
              attribute.getExternalName(),
              ValueType.COMPLEX,
              complexValue));
          final List<Property> values = complexValue.getValue();
          convertProperties(getMethod.invoke(row), values, attribute.getStructuredType());

        } else {
          if (row != null) {
            Object odataValue = getMethod.invoke(row);
            if (attribute.getConverter() != null) {
              AttributeConverter<T, S> converter = attribute.getConverter();
              odataValue = converter.convertToDatabaseColumn((T) odataValue);
            }

            properties.add(new Property(
                attribute.getExternalFQN().getFullQualifiedNameAsString(),
                attribute.getExternalName(),
                ValueType.PRIMITIVE,
                odataValue));
          } else
            properties.add(new Property(
                attribute.getExternalFQN().getFullQualifiedNameAsString(),
                attribute.getExternalName(),
                ValueType.PRIMITIVE,
                null));
        }
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  private Method getGetter(final String attributeName, final Map<String, Method> methodMap)
      throws ODataJPAQueryException {
    final String getterName = ACCESS_MODIFIER_GET + JPAEdmNameBuilder.firstToUpper(attributeName);

    if (methodMap.get(getterName) == null)
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ACCESS_NOT_FOUND,
          HttpStatusCode.INTERNAL_SERVER_ERROR, attributeName);
    Method getMethod;
    getMethod = methodMap.get(getterName);
    return getMethod;
  }

}