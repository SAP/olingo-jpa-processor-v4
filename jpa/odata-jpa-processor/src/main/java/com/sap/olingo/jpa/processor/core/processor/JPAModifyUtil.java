package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * This class provides some primitive util methods to support modifying
 * operations like create or update.
 * <p>
 * The set method shall fill an object from a given Map. JPA processor provides
 * in a Map the internal, JAVA attribute, names. Based on the JAVA naming
 * conventions the corresponding Setter is called, as long as the Setter has the
 * correct type.
 * 
 * @author Oliver Grande
 *
 */
public final class JPAModifyUtil {

  private JPAStructuredType st = null;

  /**
   * Create a filled instance of a JPA entity key.
   * <p>
   * For JPA entities having only one key, so do not use an IdClass, the
   * corresponding value in <code>jpaKeys</code> is returned
   * 
   * @param et
   * @param jpaKeys
   * @return
   * @throws ODataJPAProcessorException
   */
  public Object createPrimaryKey(final JPAEntityType et, final Map<String, Object> jpaKeys)
      throws ODataJPAProcessorException {
    try {
      if (et.getKey().size() == 1)
        return jpaKeys.get(et.getKey().get(0).getInternalName());

      final Object key = et.getKeyType().getConstructor().newInstance();
      setAttributes(jpaKeys, key);
      return key;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException | ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 
   * @param jpaAttributes
   * @param instanze
   * @throws ODataJPAProcessorException
   */
  public void setAttributes(final Map<String, Object> jpaAttributes, final Object instanze)
      throws ODataJPAProcessorException {
    Method[] methods = instanze.getClass().getMethods();
    for (Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        if (jpaAttributes.containsKey(attributeName)) {
          Object value = jpaAttributes.get(attributeName);
          if (!(value instanceof Map<?, ?>) && !(value instanceof JPARequestEntity)) {
            try {
              Class<?>[] parameters = meth.getParameterTypes();
              if (parameters.length == 1 && (value == null || value.getClass() == parameters[0])) {
                meth.invoke(instanze, value);
              }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
              throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
            }
          }
        }
      }
    }
  }

  /**
   * Fills instance and its embedded components. In case of embedded
   * components it first tries to get an existing instance. If that is non
   * provided a new one is created and set.
   * 
   * @param jpaAttributes
   * @param instanze
   * @throws ODataJPAProcessException
   */
  @SuppressWarnings("unchecked")
  public void setAttributesDeep(final Map<String, Object> jpaAttributes, final Object instanze, JPAStructuredType st)
      throws ODataJPAProcessException {
    final Method[] methods = instanze.getClass().getMethods();
    for (final Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        final String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        if (jpaAttributes.containsKey(attributeName)) {
          final Object value = jpaAttributes.get(attributeName);
          Class<?>[] parameters = meth.getParameterTypes();
          if (!(value instanceof JPARequestEntity) && parameters.length == 1) {
            try {
              if (!(value instanceof Map<?, ?>)) {
                if (value == null || value.getClass() == parameters[0]) {
                  meth.invoke(instanze, value);
                }
              } else {
                final String getterName = "g" + meth.getName().substring(1);
                final Class<?>[] parameter = new Class<?>[0];
                final Method getter = instanze.getClass().getMethod(getterName, parameter);
                Object embedded = null;
                if (getter != null)
                  embedded = instanze.getClass().getMethod(getterName, parameter).invoke(instanze);
                if (embedded == null) {
                  Constructor<?> cons = parameters[0].getConstructor(parameter);
                  embedded = cons.newInstance();
                  meth.invoke(instanze, embedded);
                }
                if (embedded != null) {
                  if (this.st == null)
                    this.st = st;
                  setAttributesDeep((Map<String, Object>) value, embedded, st.getAttribute(attributeName)
                      .getStructuredType());
                  if (this.st.equals(st)) {
                    this.st = null;
                  }
                }
              }
            } catch (IllegalAccessException | IllegalArgumentException | ODataJPAModelException
                | NoSuchMethodException | SecurityException | InstantiationException e) {
              throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
            } catch (InvocationTargetException | ODataJPAProcessException e) {
              String pathPart = null;
              if (e.getCause() instanceof ODataJPAProcessException) {
                try {
                  pathPart = st.getAttribute(attributeName).getExternalName();
                  if (this.st != null && this.st.equals(st)) {
                    String path = st.getExternalName() + "/" + pathPart + "/"
                        + ((ODataJPAInvocationTargetException) e).getPath();
                    this.st = null;
                    throw new ODataJPAInvocationTargetException(e.getCause(), path);
                  }
                } catch (ODataJPAModelException e1) {
                  throw new ODataJPAProcessorException(e1, HttpStatusCode.INTERNAL_SERVER_ERROR);
                }
                if (e instanceof ODataJPAInvocationTargetException)
                  throw new ODataJPAInvocationTargetException(e.getCause(), pathPart + "/"
                      + ((ODataJPAInvocationTargetException) e).getPath());
                else
                  throw new ODataJPAInvocationTargetException(e.getCause(), pathPart);

              } else
                throw new ODataJPAInvocationTargetException(e);
            }
          }
        }
      }
    }
  }

  /**
   * Sets a link between a source and target instance. Prerequisite are
   * existing setter and getter on the level of the sourceInstance. In case of to n associations it is expected that the
   * getter always returns a collection.
   * 
   * @param parentInstance
   * @param newInstance
   * @param pathInfo
   * @throws ODataJPAProcessorException
   */
  @SuppressWarnings("unchecked")
  public <T> void linkEnties(final Object sourceInstance, final T targetInstance, final JPAAssociationPath pathInfo)
      throws ODataJPAProcessorException {
    final String relationName = pathInfo.getPath().get(0).getInternalName();
    final String methodSuffix = relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
    try {
      if (pathInfo.isCollection()) {
        final Method getter = sourceInstance.getClass().getMethod("get" + methodSuffix);
        ((Collection<T>) getter.invoke(sourceInstance)).add(targetInstance);
      } else {
        final Method setter = sourceInstance.getClass().getMethod("set" + methodSuffix,
            targetInstance.getClass());
        setter.invoke(sourceInstance, targetInstance);
      }
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
