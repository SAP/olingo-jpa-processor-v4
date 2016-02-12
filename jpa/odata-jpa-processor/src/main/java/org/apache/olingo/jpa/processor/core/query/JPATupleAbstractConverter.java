package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.server.api.ODataApplicationException;

public abstract class JPATupleAbstractConverter {

  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";
  private static final HashMap<String, HashMap<String, Method>> methodsBuffer =
      new HashMap<String, HashMap<String, Method>>();
  protected final JPAEntityType jpaConversionTargetEntity;

  public JPATupleAbstractConverter(JPAEntityType jpaEntity) {
    super();
    this.jpaConversionTargetEntity = jpaEntity;
  }

  protected String buildConcatenatedKey(Tuple row, List<JPAOnConditionItem> joinColumns) {
    StringBuffer buffer = new StringBuffer();
    for (JPAOnConditionItem item : joinColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      buffer.append(row.get(item.getLeftPath().getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  protected Entity convertRow(JPAEntityType rowEntity, Tuple row) throws ODataApplicationException {
    Map<String, ComplexValue> complexValueBuffer = new HashMap<String, ComplexValue>();
    Entity odataEntity = new Entity();
    odataEntity.setType(rowEntity.getExternalFQN().getFullQualifiedNameAsString());
    List<Property> properties = odataEntity.getProperties();
    for (TupleElement<?> element : row.getElements()) {
      try {
        convertAttribute(row.get(element.getAlias()), element.getAlias(), "", rowEntity, complexValueBuffer,
            properties);
      } catch (ODataJPAModelException e) {
        throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
      }
    }
    try {
      odataEntity.setId(createId(jpaConversionTargetEntity.getKey(), row));
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.getStatusCode(),
          Locale.ENGLISH, e);
    }
    for (String attribute : complexValueBuffer.keySet()) {
      ComplexValue cv = complexValueBuffer.get(attribute);
      cv.getNavigationLinks().addAll(createExpand(row, odataEntity.getId(), attribute));
    }
    odataEntity.getNavigationLinks().addAll(createExpand(row, odataEntity.getId(), ""));
    return odataEntity;
  }

  protected abstract Collection<? extends Link> createExpand(Tuple row, URI id, String attribute)
      throws ODataApplicationException;

  protected abstract URI createId(List<? extends JPAAttribute> keyAttributes, Tuple row)
      throws ODataApplicationException, ODataRuntimeException;

  protected HashMap<String, Method> getGetter(JPAAttribute structuredAttribute) {
    HashMap<String, Method> pojoMethods = methodsBuffer.get(structuredAttribute.getInternalName());
    if (pojoMethods == null) {
      pojoMethods = new HashMap<String, Method>();
      Method[] allMethods = structuredAttribute.getStructuredType().getTypeClass().getMethods();
      for (Method m : allMethods) {
        pojoMethods.put(m.getName(), m);
      }
      methodsBuffer.put(structuredAttribute.getInternalName(), pojoMethods);
    }
    return pojoMethods;
  }

  void convertAttribute(Object value, String externalName, String prefix, JPAStructuredType jpaStructuredType,
      Map<String, ComplexValue> complexValueBuffer, List<Property> properties)
          throws ODataJPAModelException {

    ComplexValue compexValue = null;
    if (jpaStructuredType.getPath(externalName) != null) {
      JPAAttribute attribute = (JPAAttribute) jpaStructuredType.getPath(externalName).getPath().get(0);// getLeaf();
      if (attribute != null && attribute.isComplex()) {
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
        List<Property> values = compexValue.getValue();
        int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPERATOR.length();
        String attributeName = externalName.substring(splitIndex);
        convertAttribute(value, attributeName, bufferKey, attribute.getStructuredType(), complexValueBuffer, values);
      } else {
        // ...$select=Name1,Address/Region
        properties.add(new Property(
            null,
            externalName,
            ValueType.PRIMITIVE,
            value));
      }
    }
  }

}