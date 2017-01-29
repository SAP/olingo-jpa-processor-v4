package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPARequestEntityImpl implements JPARequestEntity {
  private final JPAEntityType et;
  private final Map<String, Object> jpaAttributes;
  private final Map<String, Object> jpaKeys;

  JPARequestEntityImpl(JPAEntityType et, Map<String, Object> jpaAttributes) {
    super();
    this.et = et;
    this.jpaAttributes = jpaAttributes;
    this.jpaKeys = new HashMap<String, Object>(0);
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public Map<String, Object> getData() {
    return jpaAttributes;
  }

  @Override
  public Map<String, Object> getKeys() {
    return jpaKeys;
  }

  @Override
  public void setAttributes(JPAStructuredType st, Map<String, Object> jpaAttributes, Object instanze)
      throws ODataJPAProcessorException {
    Method[] methods = instanze.getClass().getMethods();
    for (Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        Object value = jpaAttributes.get(attributeName);
        if (value != null) {
          try {
            if (value instanceof Map<?, ?>) {
              final JPAAttribute jpaEmbedded = st.getAttribute(attributeName);
              Method getter = findGetter(attributeName, methods);
              Object subInstance = null;
              if (getter != null) {
                subInstance = getter.invoke(instanze);
                if (subInstance == null) {
                  subInstance = createPOJO(jpaEmbedded.getStructuredType(), (Map<String, Object>) value);
                } else {
                  setAttributes(st, (Map<String, Object>) value, subInstance);
                }
              } else
                subInstance = createPOJO(jpaEmbedded.getStructuredType(), (Map<String, Object>) value);
              meth.invoke(instanze, subInstance);
            } else
              meth.invoke(instanze, value);
          } catch (IllegalAccessException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (IllegalArgumentException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (InvocationTargetException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (ODataJPAModelException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
        } else {
          try {
            final JPAAttribute attribute = st.getAttribute(attributeName);
            if (attribute != null && attribute.isComplex() && attribute.isKey()) {
              meth.invoke(instanze, createPOJO(attribute.getStructuredType(), jpaAttributes));
            }
          } catch (ODataJPAModelException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (IllegalAccessException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (IllegalArgumentException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          } catch (InvocationTargetException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
        }
      }
    }
  }

  private Method findGetter(String attributeName, Method[] methods) {
    final String getterName = "get" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
    Method method = null;
    for (Method meth : methods) {
      if (getterName.equals(meth.getName())) {
        method = meth;
        break;
      }
    }
    return method;
  }

  private Object createInstance(Constructor<?> cons) throws ODataJPAProcessorException {

    try {
      Object instanze = cons.newInstance();
      return instanze;
    } catch (InstantiationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalAccessException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalArgumentException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  private Constructor<?> getConstructor(JPAStructuredType st) {
    Constructor<?> cons = null;
    Constructor<?>[] constructors = st.getTypeClass().getConstructors();
    for (int i = 0; i < constructors.length; i++) {
      cons = constructors[i];
      if (cons.getParameterCount() == 0) {
        break;
      }
    }
    return cons;
  }

  private Object createPOJO(JPAStructuredType st, Map<String, Object> jpaAttributes) throws ODataJPAProcessorException {
    final Constructor<?> cons = getConstructor(st);
    final Object instanze = createInstance(cons);
    setAttributes(st, jpaAttributes, instanze);
    for (final String attributeName : jpaAttributes.keySet()) {
      if (jpaAttributes.get(attributeName) instanceof JPARequestEntity) {
        final JPARequestEntity navigationTarget = (JPARequestEntity) jpaAttributes.get(attributeName);
        final Object linkedEntity = createPOJO(navigationTarget.getEntityType(), navigationTarget.getData());
        try {
          @SuppressWarnings("unused")
          JPAAssociationAttribute a = st.getAssociation(attributeName);
        } catch (ODataJPAModelException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return instanze;
  }
}
