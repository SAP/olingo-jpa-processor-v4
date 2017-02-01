package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public final class JPAModifyUtil {

  public void setAttributes(final Map<String, Object> jpaAttributes, final Object instanze)
      throws ODataJPAProcessorException {
    Method[] methods = instanze.getClass().getMethods();
    for (Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("set")) {
        String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        Object value = jpaAttributes.get(attributeName);
        if (value != null) {
          if (!(value instanceof Map<?, ?>)) {
            try {
              meth.invoke(instanze, value);
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
  }
}
