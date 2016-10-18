package org.apache.olingo.jpa.processor.core.processor;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import org.apache.olingo.jpa.processor.core.query.ExpressionUtil;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.prefer.Preferences;
import org.apache.olingo.server.api.prefer.Preferences.Return;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.org.jpa.processor.core.converter.JPAExpandResult;
import org.apache.org.jpa.processor.core.converter.JPATupleResultConverter;

public class JPACUDRequestProcessor extends JPAAbstractRequestProcessor {

  private final JPAODataSessionContextAccess sessionContext;
  private final ServiceMetadata serviceMetadata;

  public JPACUDRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess sessionContext, final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    super(odata, sessionContext, requestContext);
    this.sessionContext = sessionContext;
    this.serviceMetadata = serviceMetadata;
  }

  public void createEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    Map<String, Object> jpaAttributes = new HashMap<String, Object>();

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();

    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      jpaAttributes = convertProperty(et, requestEntity.getProperties());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    // Create entity
    Object primaryKey = null;
    em.getTransaction().begin();
    try {
      primaryKey = handler.createEntity(et, jpaAttributes, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();

    // Create response
    // 11.4.2 Create an Entity states:
    // Upon successful completion, the response MUST contain a Location header that contains the edit URL or read URL of
    // the created entity.
    //
    // 8.3.2 Header Location
    // The Location header MUST be returned in the response from a Create Entity or Create Media Entity request to
    // specify the edit URL, or for read-only entities the read URL, of the created entity, and in responses returning
    // 202 Accepted to specify the URL that the client can use to request the status of an asynchronous request.
    //
    // 8.2.8.7 Preference return=representation and return=minimal states:
    // A preference of return=minimal requests that the service invoke the request but does not return content in the
    // response. The service MAY apply this preference by returning 204 No Content in which case it MAY include a
    // Preference-Applied response header containing the return=minimal preference.
    // A preference of return=representation requests that the service invokes the request and returns the modified
    // resource. The service MAY apply this preference by returning the representation of the successfully modified
    // resource in the body of the response, formatted according to the rules specified for the requested format. In
    // this case the service MAY include a Preference-Applied response header containing the return=representation
    // preference.

    Preferences prefer = odata.createPreferences(request.getHeaders((HttpHeader.PREFER)));

    if (prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
    } else {
      Object newEntity = em.find(et.getTypeClass(), primaryKey);
      em.refresh(newEntity);
      Entity createdEntity = convertEntity(et, newEntity);
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResonce(response, responseFormat, serializer.serialize(request, entities));
    }
  }

  /*
   * 4.4 Addressing References between Entities
   * DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
   * DELETE http://host/service/Products(0)/Category/$ref
   */
  public void deleteEntity(final ODataResponse response) throws ODataJPAProcessException {
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<String, Object>();

    // 1. Retrieve the entity set which belongs to the requested entity
    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 2. Convert Key from URL to JPA
    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      List<UriParameter> uriKeyPredicates = uriResourceEntitySet.getKeyPredicates();
      for (UriParameter uriParam : uriKeyPredicates) {
        JPAAttribute attribute = et.getPath(uriParam.getName()).getLeaf();
        jpaKeyPredicates.put(attribute.getInternalName(), ExpressionUtil.convertValueOnAttribute(odata, attribute,
            uriParam.getText(), true));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    // 3. Perform Delete
    em.getTransaction().begin();
    try {
      handler.deleteEntity(et, jpaKeyPredicates, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();

    // 4. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }

  Entity convertEntity(JPAEntityType et, Object jpaEntity) throws ODataJPAProcessorException {
    JPATupleResultConverter converter;
    try {
      converter = new JPATupleResultConverter(sd, new JPAEntityResult(et, jpaEntity), odata
          .createUriHelper(), serviceMetadata);
      return converter.getResult().getEntities().get(0);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (ODataApplicationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  //
  private Map<String, Object> convertProperty(final JPAStructuredType st, List<Property> odataProperties)
      throws ODataJPAModelException, ODataJPAProcessorException, ODataJPAFilterException {

    final Map<String, Object> jpaAttributes = new HashMap<String, Object>();
    String internalName;
    Object jpaAttribute = null;
    for (Property odataProperty : odataProperties) {
      switch (odataProperty.getValueType()) {
      case COMPLEX:
        JPAPath path = st.getPath(odataProperty.getName());
        internalName = path.getPath().get(0).getInternalName();
        JPAStructuredType a = st.getAttribute(internalName).getStructuredType();

        jpaAttribute = convertProperty(a, ((ComplexValue) odataProperty.getValue()).getValue());
        break;
      case PRIMITIVE:
        final JPAAttribute attribute = st.getPath(odataProperty.getName()).getLeaf();
        internalName = attribute.getInternalName();
        jpaAttribute = ExpressionUtil.convertValueOnAttribute(odata, attribute, odataProperty.getValue().toString(),
            false);
        break;
      default:
        throw new ODataJPAProcessorException(MessageKeys.NOT_SUPPORTED_PROP_TYPE, HttpStatusCode.NOT_IMPLEMENTED,
            odataProperty.getValueType().name());
      }
      jpaAttributes.put(internalName, jpaAttribute);
    }
    return jpaAttributes;
  }

  private class JPAEntityResult implements JPAExpandResult {
    private final Object jpaEntity;
    private final JPAEntityType et;
    private final Map<JPAAssociationPath, JPAExpandResult> children;
    private final List<JPAPath> pathList;

    public JPAEntityResult(JPAEntityType et, Object jpaEntity) throws ODataJPAModelException {
      this.jpaEntity = jpaEntity;
      this.et = et;
      this.children = new HashMap<JPAAssociationPath, JPAExpandResult>(0);
      this.pathList = et.getPathList();
    }

    @Override
    public List<Tuple> getResult(String key) {
      final Map<String, Object> getterMap = new HashMap<String, Object>();
      // TODO Handle description fields
      Method[] methods = jpaEntity.getClass().getMethods();
      for (Method meth : methods) {
        if (meth.getName().substring(0, 3).equals("get")) {
          String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
          try {
            Object value = meth.invoke(jpaEntity);
            getterMap.put(attributeName, value);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }

      List<Tuple> result = new ArrayList<Tuple>();
      JPATuple tuple = new JPATuple();
      result.add(tuple);

      for (JPAPath path : pathList) {
        if (path.getPath().size() == 1) {
          Object value = getterMap.get(path.getLeaf().getInternalName());
          tuple.addElement(path.getAlias(), path.getLeaf().getType(), value);
        }
      }

      // .indexOf(alias);

      return result;
    }

    @Override
    public Map<JPAAssociationPath, JPAExpandResult> getChildren() {
      return children;
    }

    @Override
    public boolean hasCount() {
      return false;
    }

    @Override
    public Integer getCount() {
      return null;
    }

    @Override
    public JPAEntityType getEntityType() {
      return et;
    }

  }

  private class JPATuple implements Tuple {
    private List<TupleElement<?>> elements = new ArrayList<TupleElement<?>>();
    private Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public <X> X get(TupleElement<X> arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    public void addElement(String alias, Class<?> javaType, Object value) {
      elements.add(new JPATupleElement<Object>(alias, javaType));
      values.put(alias, value);

    }

    /**
     * Get the value of the tuple element to which the
     * specified alias has been assigned.
     * @param alias alias assigned to tuple element
     * @return value of the tuple element
     * @throws IllegalArgumentException if alias
     * does not correspond to an element in the
     * query result tuple
     */
    @Override
    public Object get(String alias) {
      return values.get(alias);
    }

    @Override
    public Object get(int arg0) {
      assert 1 == 2;
      return null;
    }

    @Override
    public <X> X get(String arg0, Class<X> arg1) {
      assert 1 == 2;
      return null;
    }

    @Override
    public <X> X get(int arg0, Class<X> arg1) {
      assert 1 == 2;
      return null;
    }

    @Override
    public List<TupleElement<?>> getElements() {
      return elements;
    }

    @Override
    public Object[] toArray() {
      return null;
    }

  }

  private class JPATupleElement<X> implements TupleElement<X> {

    private final String alias;
    private final Class<? extends X> javaType;

    public JPATupleElement(String alias, Class<? extends X> javaType) {
      this.alias = alias;
      this.javaType = javaType;
    }

    @Override
    public String getAlias() {
      return alias;
    }

    @Override
    public Class<? extends X> getJavaType() {
      return javaType;
    }

  }

}
