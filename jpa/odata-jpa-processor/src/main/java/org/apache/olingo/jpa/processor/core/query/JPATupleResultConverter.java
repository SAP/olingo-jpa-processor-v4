package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPATupleResultConverter extends JPATupleAbstractConverter {
  private final JPAExpandResult jpaQueryResult;
  private final EdmEntitySet edmEntitySet;

  public JPATupleResultConverter(EdmEntitySet entitySet, ServicDocument sd, JPAExpandResult jpaQueryResult)
      throws ODataJPAModelException {
    super(sd.getEntity(entitySet.getName()), jpaQueryResult);
    this.jpaQueryResult = jpaQueryResult;
    this.edmEntitySet = entitySet;
  }

  public EntityCollection getResult() throws ODataApplicationException {
    EntityCollection odataEntityCollection = new EntityCollection();
    List<Entity> odataResults = odataEntityCollection.getEntities();

    for (Tuple row : jpaQueryResult.getResult("root")) {
      Entity odataEntity = convertRow(jpaConversionTargetEntity, row);
      odataResults.add(odataEntity);
    }
    return odataEntityCollection;
  }

  @Override
  protected URI createId(List<? extends JPAAttribute> keyAttributes, Tuple row) throws ODataApplicationException,
      ODataRuntimeException {

    try {
      StringBuffer uriString = new StringBuffer(edmEntitySet.getName());
      uriString.append("(");
      if (keyAttributes.size() == 1)
        try {
          createIdSimple(keyAttributes.get(0), uriString, row.get(keyAttributes.get(0).getExternalName()));
        } catch (ODataApplicationException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      else if (keyAttributes.size() >= 1)
        createIdCompund(keyAttributes, uriString, row);
      uriString.append(")");
      return new URI(uriString.toString());

    } catch (URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmEntitySet, e);
    } catch (IllegalArgumentException e) {
      return null;
      // throw new ODataRuntimeException("Key not found: " + resultEdmEntitySet, e);
    } catch (ODataJPAModelException e) {
      throw new ODataRuntimeException("Mapping error for key property", e);
    }

  }

  private void createIdCompund(List<? extends JPAAttribute> keyAttributes, StringBuffer uriString, Tuple row)
      throws ODataJPAModelException, ODataApplicationException {

    for (JPAAttribute attribute : keyAttributes) {
      uriString.append(attribute.getExternalName());
      uriString.append("=");
      createIdSimple(attribute, uriString, row.get(attribute.getExternalName()));
      uriString.append(",");
    }
    uriString.delete(uriString.length() - 1, uriString.length());
  }

  private void createIdCompundEmbedded(JPAAttribute keyAttribute, StringBuffer uriString, Object value,
      String prefix) throws ODataApplicationException {
    HashMap<String, Method> methodList = getGetter(keyAttribute);

    try {
      for (JPAPath keyPath : keyAttribute.getStructuredType().getPathList()) {
        JPAAttribute keySubAttribute = (JPAAttribute) keyPath.getLeaf();
        uriString.append(prefix);
        uriString.append(JPAPath.PATH_SEPERATOR);
        uriString.append(keySubAttribute.getExternalName());
        uriString.append("=");
        // TODO Nested embedded keys and mixed embedded - not embedded Keys
        String getter = ACCESS_MODIFIER_GET + JPAEdmNameBuilder.firstToUpper(keySubAttribute.getInternalName());
        Method get = methodList.get(getter);
        if (get == null)
          throw new ODataApplicationException("Getter not found for " + keySubAttribute.getExternalName(),
              HttpStatusCode.INTERNAL_SERVER_ERROR.ordinal(),
              Locale.ENGLISH);

        createIdSimple(keySubAttribute, uriString, get.invoke(value));
        uriString.append(",");
      }
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (IllegalAccessException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (IllegalArgumentException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (InvocationTargetException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    }
    uriString.delete(uriString.length() - 1, uriString.length());

  }

  private void createIdOneValue(JPAAttribute keyAttribute, final StringBuffer uriString, final Object value)
      throws ODataJPAModelException {
    if (keyAttribute.getEdmType() == EdmPrimitiveTypeKind.String) {
      // TODO Check which types have to be in "'"
      uriString.append("'");
      uriString.append(value);
      uriString.append("'");
    } else
      uriString.append(value);
  }

  private void createIdSimple(JPAAttribute keyAttribute, final StringBuffer uriString, final Object value)
      throws ODataJPAModelException, ODataApplicationException {

    if (keyAttribute.isComplex()) {
      List<JPAPath> attributes = keyAttribute.getStructuredType().getPathList();
      if (attributes.size() == 1)
        createIdSimple((JPAAttribute) attributes.get(0).getLeaf(), uriString, value);
      else
        createIdCompundEmbedded(keyAttribute, uriString, value, keyAttribute.getExternalName());
    } else {
      createIdOneValue(keyAttribute, uriString, value);
    }
  }
}
