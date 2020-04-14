package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

public final class JPARequestLinkImpl implements JPARequestLink {

  private final JPAAssociationPath path;
  private final String bindingLink;
  private final Map<String, Object> keys;
  private final Map<String, Object> values;
  private final JPAConversionHelper helper;

  JPARequestLinkImpl(final JPAAssociationPath path, final String bindingLink, final JPAConversionHelper helper) {
    super();
    this.path = path;
    this.bindingLink = bindingLink;
    this.helper = helper;
    this.keys = new HashMap<>();
    this.values = new HashMap<>();
  }

  @Override
  public JPAEntityType getEntityType() {
    return (JPAEntityType) path.getTargetType();
  }

  @Override
  public Map<String, Object> getRelatedKeys() throws ODataJPAProcessorException {
    if (keys.size() == 0) try {
      buildKeys();
    } catch (Exception e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
    return keys;
  }

  @Override
  public Map<String, Object> getValues() throws ODataJPAProcessorException {
    if (values.size() == 0) try {
      buildKeys();
    } catch (Exception e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
    return values;
  }

  private void buildKeys() throws ODataJPAModelException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException, EdmPrimitiveTypeException {
    OData odata = OData.newInstance();

    // TODO replace by Olingo OData Util
    final String[] entityTypeAndKey = bindingLink.split("[\\(\\)]");
    final String[] keyElements = entityTypeAndKey[1].split("[,=]");
    if (keyElements.length > 1) {
      for (int i = 0; i < keyElements.length; i += 2) {
        for (JPAOnConditionItem item : path.getJoinColumnsList()) {
          if (item.getLeftPath().getLeaf().getExternalName().equals(keyElements[i])) {

            keys.put(item.getLeftPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[i + 1], item
                .getLeftPath()));
            values.put(item.getRightPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[i + 1], item
                .getRightPath()));
            break;
          }
          if (item.getRightPath().getLeaf().getExternalName().equals(keyElements[i])) {
            keys.put(item.getRightPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[i + 1], item
                .getRightPath()));
            values.put(item.getLeftPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[i + 1], item
                .getLeftPath()));
            break;
          }
        }
      }
    } else {
      // If an entity has only one key property, it is allowed to omit the property name
      List<JPAAttribute> targetKeys = ((JPAEntityType) path.getTargetType()).getKey();
      String attributeName = targetKeys.get(0).getInternalName();
      JPAOnConditionItem item = path.getJoinColumnsList().get(0);
      if (item.getRightPath().getLeaf().getInternalName().equals(attributeName)) {
        keys.put(item.getRightPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[0], item
            .getRightPath()));
        values.put(item.getLeftPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[0], item
            .getLeftPath()));
        return;
      }
      if (item.getLeftPath().getLeaf().getInternalName().equals(attributeName)) {
        keys.put(item.getLeftPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[0], item
            .getLeftPath()));
        values.put(item.getRightPath().getLeaf().getInternalName(), convertKeyValue(odata, keyElements[0], item
            .getRightPath()));
        return;
      }
    }
  }

  private Object convertKeyValue(final OData odata, final String keyElementValue, final JPAPath path)
      throws ODataJPAModelException, NoSuchMethodException, InstantiationException, IllegalAccessException,
      InvocationTargetException, EdmPrimitiveTypeException {

    EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(path.getLeaf().getEdmType());
    final Class<?> defaultType = edmType.getDefaultType();
    final Constructor<?> c = defaultType.getConstructor(String.class);
    final Object value = c.newInstance(edmType.fromUriLiteral(keyElementValue));
    return helper.processAttributeConverter(value, path.getLeaf());
  }

}
