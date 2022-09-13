package com.sap.olingo.jpa.processor.core.processor;

import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

/**
 * Reflection API does not provide parameter names of the constructor => No name matching possible, so in case multiple
 * keys have the same type can not be distinguished. Therefore priority is follows:
 * follows:
 * <ol>
 * <li>Constructor with one parameter taking an instance of the IdClass if used</li>
 * <li>Constructor with one parameter in case the entity has a single key and type is matching</li> *
 * <li>Constructor without parameter and using setter to fill the key</li>
 * </ol>
 * Id Class Constructor without parameter and using setter to fill the key;
 * @author Oliver Grande
 * @since 1.10.0
 * 08.09.2022
 * @param <E> JPA Entity
 */
class JPAInstanceCreator<E extends JPAEntityType> {
  private static final Log LOGGER = LogFactory.getLog(JPAInstanceCreator.class);
  private final JPAConversionHelper helper;
  private final JPAModifyUtil util;
  private final E type;
  private final OData odata;
  private final Class<?> entityClass;

  JPAInstanceCreator(final OData odata, final E type) {
    super();
    this.type = type;
    this.odata = odata;
    // Take the constructor of the original binding parameter, as Olingo may have selected an action having a super
    // type has binding parameter and the corresponding Java class is abstract.
    this.entityClass = type.getTypeClass();
    this.helper = new JPAConversionHelper();
    this.util = new JPAModifyUtil();
  }

  /**
   * Preference:
   * <p>
   * - Constructor taking an instance of the idClass<br>
   * - Constructor taking the key attributes<br>
   * - Constructor without parameter<br>
   * @param <T>
   * @return
   * @throws ODataJPAProcessorException
   */
  @SuppressWarnings("unchecked")
  <T> Optional<Constructor<T>> determinePreferedConstructor() throws ODataJPAProcessorException {
    try {
      Constructor<T> result = null;
      final Constructor<T>[] constructors = (Constructor<T>[]) entityClass.getConstructors();
      for (final Constructor<T> c : constructors) {
        if (preferedCandidate(result, c))
          result = c;
      }
      return Optional.ofNullable(result);
    } catch (final SecurityException | ODataJPAModelException e) {
      LOGGER.error("Error while determine constructor for binding parameter");
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    }
  }

  Optional<Object> createInstance(final List<UriParameter> keyPredicates) throws ODataJPAProcessorException {
    final Optional<Constructor<Object>> c = determinePreferedConstructor();
    try {
      if (c.isPresent()) {
        final Map<String, Object> jpaAttributes = helper.convertUriKeys(odata, type, keyPredicates);
        if (c.get().getParameterCount() == 0) {
          final Object param = c.get().newInstance();
          util.setAttributesDeep(jpaAttributes, param, type);
          return Optional.of(param);
        } else if (type.hasCompoundKey()) {
          final Constructor<?> keyConstructor = type.getKeyType().getConstructor();
          final Object key = keyConstructor.newInstance();
          if (hasSetterForKeys(type.getKeyType())) {
            util.setAttributesDeep(jpaAttributes, key, type);
            return Optional.of(c.get().newInstance(key));
          }
        } else {
          return Optional.of(c.get().newInstance(jpaAttributes.values().toArray()));
        }
      }
    } catch (ODataJPAFilterException | ODataJPAProcessorException | ODataJPAInvocationTargetException
        | ODataJPAModelException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return Optional.empty();
  }

  private <T> boolean preferedCandidate(final Constructor<T> current, final Constructor<T> candidate)
      throws ODataJPAModelException {
    if (type.hasCompoundKey()) {
      final Class<?> idClass = type.getKeyType();
      if (candidate.getParameterTypes().length == 1 && candidate.getParameterTypes()[0] == idClass)
        return true;
    } else if (candidate.getParameterCount() == 1
        && candidate.getParameterTypes()[0].equals(type.getKeyType())) {
      return true;
    }
    if (candidate.getParameterCount() == 0
        && !(current != null && current.getParameterCount() == 1 && type.hasCompoundKey())) {
      return hasSetterForKeys(type.getTypeClass());
    }
    return false;
  }

  private boolean hasSetterForKeys(final Class<?> typeClass) throws ODataJPAModelException {
    return util.buildSetterList(typeClass, type.getKey()).values().stream().noneMatch(Objects::isNull);
  }
}
