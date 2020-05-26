package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;

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

  public String buildMethodNameSuffix(final JPAElement pathItem) {
    final String relationName = pathItem.getInternalName();
    return relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
  }

  /**
   * Create a filled instance of a JPA entity key.
   * <br>
   * For JPA entities having only one key, so do not use an IdClass, the
   * corresponding value in <code>jpaKeys</code> is returned
   * 
   * @param et
   * @param jpaKeys
   * @return
   * @throws ODataJPAProcessorException
   */
  public Object createPrimaryKey(final JPAEntityType et, final Map<String, Object> jpaKeys, final JPAStructuredType st)
      throws ODataJPAProcessorException, ODataJPAInvocationTargetException {
    try {
      if (et.getKey().size() == 1)
        return jpaKeys.get(et.getKey().get(0).getInternalName());

      final Object key = et.getKeyType().getConstructor().newInstance();
      setAttributes(jpaKeys, key, st);
      return key;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException | ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 
   * @param et
   * @param instance
   * @return
   * @throws ODataJPAProcessorException
   * @throws ODataJPAInvocationTargetException
   */
  public Object createPrimaryKey(final JPAEntityType et, final Object instance) throws ODataJPAProcessorException {
    try {
      if (et.getKey().size() == 1)
        return getAttribute(instance, et.getKey().get(0));

      final Object key = et.getKeyType().getConstructor().newInstance();
      for (final JPAAttribute keyElement : et.getKey()) {
        setAttribute(key, keyElement, getAttribute(instance, keyElement));
      }

      return key;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException | ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Sets a link between a source and target instance. Prerequisite are
   * existing setter and getter on the level of the sourceInstance. In case of to n associations it is expected that the
   * getter always returns a collection. In case structured properties are passed either a getter returns always an
   * instance or the corresponding type has a parameter less constructor.
   * 
   * @param parentInstance
   * @param newInstance
   * @param pathInfo
   * @throws ODataJPAProcessorException
   */

  public <T> void linkEntities(final Object sourceInstance, final T targetInstance, final JPAAssociationPath pathInfo)
      throws ODataJPAProcessorException {

    try {
      final Object source = determineSourceForLink(sourceInstance, pathInfo);
      setLink(source, targetInstance, pathInfo.getLeaf());

    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Fills instance without filling its embedded components.
   * 
   * @param jpaAttributes Map of attributes and values that shall be changed
   * @param instanze JPA POJO instance to take the changes
   * @param st Entity Type
   * @throws ODataJPAProcessorException Thrown when ever a problem with invoking a getter or setter occurs except
   * InvocationTargetException occurs.
   * @throws ODataJPAInvocationTargetException Thrown when InvocationTargetException was thrown.
   * ODataJPAInvocationTargetException contains the original cause and the OData path to the property which should be
   * changed. The path starts with the entity type. The path parts a separated by {@code JPAPath.PATH_SEPERATOR}.
   */
  public void setAttributes(final Map<String, Object> jpaAttributes, final Object instanze, final JPAStructuredType st)
      throws ODataJPAProcessorException, ODataJPAInvocationTargetException {
    final Method[] methods = instanze.getClass().getMethods();
    for (final Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        final String attributeName = meth.getName().substring(3, 4).toLowerCase(Locale.ENGLISH) + meth.getName()
            .substring(4);
        if (jpaAttributes.containsKey(attributeName)) {
          final Object value = jpaAttributes.get(attributeName);
          if (!(value instanceof Map<?, ?>) && !(value instanceof JPARequestEntity)) {
            try {
              final Class<?>[] parameters = meth.getParameterTypes();
              if (parameters.length == 1 && (value == null || value.getClass() == parameters[0])) {
                meth.invoke(instanze, value);
              }
            } catch (IllegalAccessException | IllegalArgumentException e) {
              throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
            } catch (final InvocationTargetException e) {
              try {
                throw new ODataJPAInvocationTargetException(e.getCause(),
                    st.getExternalName() + JPAPath.PATH_SEPARATOR + st.getAttribute(attributeName)
                        .orElseThrow(() -> new ODataJPAProcessorException(
                            ATTRIBUTE_NOT_FOUND, HttpStatusCode.INTERNAL_SERVER_ERROR, attributeName))
                        .getExternalName());
              } catch (final ODataJPAModelException e1) {
                throw new ODataJPAProcessorException(e1, HttpStatusCode.INTERNAL_SERVER_ERROR);
              }
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
   * @param jpaAttributes Map of attributes and values that shall be changed
   * @param instanze JPA POJO instance to take the changes
   * @param st Entity Type
   * @throws ODataJPAProcessorException Thrown when ever a problem with invoking a getter or setter occurs except
   * InvocationTargetException occurs.
   * @throws ODataJPAInvocationTargetException Thrown when InvocationTargetException was thrown.
   * ODataJPAInvocationTargetException contains the original cause and the OData path to the property which should be
   * changed. The path starts with the entity type. The path parts a separated by {@code JPAPath.PATH_SEPERATOR}.
   */
  public void setAttributesDeep(final Map<String, Object> jpaAttributes, final Object instanze,
      final JPAStructuredType st) throws ODataJPAProcessorException, ODataJPAInvocationTargetException {

    final Method[] methods = instanze.getClass().getMethods();
    for (final Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        final String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        if (jpaAttributes.containsKey(attributeName)) {
          final Object value = jpaAttributes.get(attributeName);
          final Class<?>[] parameters = meth.getParameterTypes();
          if (!(value instanceof JPARequestEntity) && parameters.length == 1) {
            setAttributeDeep(instanze, st, meth, attributeName, value, parameters);
          }
        }
      }
    }
  }

  /**
   * Boxed
   * missing getter
   * @param parentInstance
   * @param newInstance
   * @param pathInfo
   * @throws ODataJPAProcessorException
   */
  public void setForeignKey(final Object parentInstance, final Object newInstance, final JPAAssociationPath pathInfo)
      throws ODataJPAProcessorException {
    try {
      for (final JPAOnConditionItem joinCloumn : pathInfo.getJoinColumnsList()) {
        setAttribute(newInstance, joinCloumn.getRightPath().getLeaf(), getAttribute(parentInstance, joinCloumn
            .getLeftPath().getLeaf()));
      }
    } catch (ODataJPAModelException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Creates an instance of type <code>type</code> using a parameter free constructor
   * @param type
   * @return
   * @throws NoSuchMethodException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private Object createInstance(final Class<?> type) throws NoSuchMethodException,
      InstantiationException, IllegalAccessException, InvocationTargetException {

    final Class<?>[] parameter = new Class<?>[0];
    final Constructor<?> cons = type.getConstructor(parameter);
    return cons.newInstance();
  }

  /**
   * Determines the instance a link shall be added to. This may be the entity or a structured type. If the structured
   * property does not exists, the method creates a new instance.
   * @param sourceInstance
   * @param pathInfo
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws ODataJPAProcessorException
   */
  private Object determineSourceForLink(final Object sourceInstance, final JPAAssociationPath pathInfo)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ODataJPAProcessorException {

    Object source = sourceInstance;
    for (final JPAElement pathItem : pathInfo.getPath()) {
      if (pathItem != pathInfo.getLeaf()) {
        final String methodSuffix = buildMethodNameSuffix(pathItem);
        final Method getter = source.getClass().getMethod("get" + methodSuffix);
        Object next = getter.invoke(source);
        if (next == null) {
          try {
            final Constructor<?> c = ((JPAAttribute) pathItem).getStructuredType().getTypeClass().getConstructor();
            next = c.newInstance();
            final Method setter = source.getClass().getMethod("set" + methodSuffix, next.getClass());
            setter.invoke(source, next);
          } catch (ODataJPAModelException | InstantiationException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
        }
        source = next;
      }
    }
    return source;
  }

  /**
   * Tries to read the current state of an attribute. If no getter exists an exception is thrown.
   * @param instance
   * @param attribute
   * @return
   * @throws NoSuchMethodException
   * @throws ODataJPAProcessorException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private Object getAttribute(final Object instance, final JPAElement attribute) throws NoSuchMethodException,
      ODataJPAProcessorException, IllegalAccessException, InvocationTargetException {

    final Method getter = instance.getClass().getMethod("get" + buildMethodNameSuffix(attribute));
    if (getter == null)
      throw new ODataJPAProcessorException(MessageKeys.GETTER_NOT_FOUND, HttpStatusCode.INTERNAL_SERVER_ERROR,
          buildMethodNameSuffix(attribute), instance.getClass().getName());
    return getter.invoke(instance);
  }

  private void handleInvocationTargetException(final JPAStructuredType st, final String attributeName,
      final Exception e)
      throws ODataJPAInvocationTargetException, ODataJPAProcessorException {

    String pathPart = null;
    try {
      pathPart = st.getAttribute(attributeName).orElseThrow(() -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND,
          HttpStatusCode.INTERNAL_SERVER_ERROR, attributeName)).getExternalName();
      if (this.st != null && this.st.equals(st)) {
        final String path = st.getExternalName() + JPAPath.PATH_SEPARATOR + pathPart + JPAPath.PATH_SEPARATOR
            + ((ODataJPAInvocationTargetException) e).getPath();
        this.st = null;
        throw new ODataJPAInvocationTargetException(e.getCause(), path);
      }
    } catch (final ODataJPAModelException e1) {
      throw new ODataJPAProcessorException(e1, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (e instanceof ODataJPAInvocationTargetException)
      throw new ODataJPAInvocationTargetException(e.getCause(), pathPart + JPAPath.PATH_SEPARATOR
          + ((ODataJPAInvocationTargetException) e).getPath());
    else
      throw new ODataJPAInvocationTargetException(e.getCause(), pathPart);
  }

  /**
   * Tries to read the current state of an attribute. If no getter exists null is returned.
   * @param instance
   * @param attribute
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private Object readCurrentState(final Object instance, final JPAElement attribute) throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {

    final Method getter = instance.getClass().getMethod("get" + buildMethodNameSuffix(attribute));
    if (getter == null)
      return null;
    return getter.invoke(instance);
  }

  private void setAttribute(final Object instance, final JPAElement attribute, final Object value)
      throws NoSuchMethodException, ODataJPAProcessorException, IllegalAccessException, InvocationTargetException {

    final Method setter = instance.getClass().getMethod("set" + buildMethodNameSuffix(attribute), value.getClass());
    if (setter == null)
      throw new ODataJPAProcessorException(MessageKeys.SETTER_NOT_FOUND, HttpStatusCode.INTERNAL_SERVER_ERROR,
          buildMethodNameSuffix(attribute), instance.getClass().getName(), value.getClass().getName());
    setter.invoke(instance, value);
  }

  private void setAttributeDeep(final Object instanze, final JPAStructuredType st, final Method meth,
      final String attributeName, final Object value, final Class<?>[] parameters) throws ODataJPAProcessorException,
      ODataJPAInvocationTargetException {
    try {
      final JPAAttribute attribute = st.getAttribute(attributeName).orElseThrow(
          () -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND,
              HttpStatusCode.INTERNAL_SERVER_ERROR, attributeName));
      if (!attribute.isComplex() || value == null) {
        if (value == null || parameters[0].isAssignableFrom(value.getClass())) {
          meth.invoke(instanze, value);
        }
      } else if (attribute.isCollection()) {
        setEmbeddedCollectionAttributeDeep(instanze, st, meth, value, parameters, attribute);
      } else {
        setEmbeddedAttributeDeep(instanze, st, meth, value, parameters, attribute);
      }
    } catch (IllegalAccessException | IllegalArgumentException | ODataJPAModelException
        | NoSuchMethodException | SecurityException | InstantiationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (InvocationTargetException | ODataJPAInvocationTargetException e) {
      handleInvocationTargetException(st, attributeName, e);
    }
  }

  @SuppressWarnings("unchecked")
  private void setEmbeddedAttributeDeep(final Object instanze, final JPAStructuredType st, final Method meth,
      final Object value, final Class<?>[] parameters, final JPAAttribute attribute)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException,
      ODataJPAModelException, ODataJPAProcessorException, ODataJPAInvocationTargetException {

    Object embedded = readCurrentState(instanze, attribute);
    if (embedded == null) {
      embedded = createInstance(parameters[0]);
      meth.invoke(instanze, embedded);
    }
    if (embedded != null) {
      if (this.st == null)
        this.st = st;
      setAttributesDeep((Map<String, Object>) value, embedded, attribute.getStructuredType());
      if (this.st.equals(st)) {
        this.st = null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void setEmbeddedCollectionAttributeDeep(final Object instanze, final JPAStructuredType st, final Method meth,
      final Object value, final Class<?>[] parameters, final JPAAttribute attribute)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException,
      ODataJPAModelException, ODataJPAProcessorException, ODataJPAInvocationTargetException {

    Collection<Object> embedded = (Collection<Object>) readCurrentState(instanze, attribute);
    if (embedded == null) {
      // List; Set; Queue
      if (parameters[0].isAssignableFrom(List.class)) {
        embedded = (Collection<Object>) createInstance(ArrayList.class);
      } else {
        embedded = (Collection<Object>) createInstance(parameters[0]);
      }
      meth.invoke(instanze, embedded);
    }
    if (embedded != null) {
      if (this.st == null)
        this.st = st;
      embedded.clear();
      for (final Map<String, Object> collectionElement : (Collection<Map<String, Object>>) value) {
        final Object line = createInstance(attribute.getStructuredType().getTypeClass());
        setAttributesDeep(collectionElement, line, attribute.getStructuredType());
        embedded.add(line);
      }
      if (this.st.equals(st)) {
        this.st = null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void setLink(final Object sourceInstance, final T targetInstance, final JPAAssociationAttribute attribute)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ODataJPAProcessorException {

    final String methodSuffix = attribute.getInternalName().substring(0, 1).toUpperCase() + attribute.getInternalName()
        .substring(1);

    if (attribute.isCollection()) {
      final Method getter = sourceInstance.getClass().getMethod("get" + methodSuffix);
      ((Collection<T>) getter.invoke(sourceInstance)).add(targetInstance);
    } else {
      Method setter = null;
      Class<?> clazz = targetInstance.getClass();
      while (clazz != null && setter == null) {
        try {
          setter = sourceInstance.getClass().getMethod("set" + methodSuffix, clazz);
        } catch (final NoSuchMethodException e) {
          clazz = clazz.getSuperclass();
        }
      }
      if (setter == null)
        throw new ODataJPAProcessorException(MessageKeys.SETTER_NOT_FOUND, HttpStatusCode.INTERNAL_SERVER_ERROR, "set"
            + methodSuffix, sourceInstance.getClass().getName(), targetInstance.getClass().getName());
      setter.invoke(sourceInstance, targetInstance);
    }
  }
}
