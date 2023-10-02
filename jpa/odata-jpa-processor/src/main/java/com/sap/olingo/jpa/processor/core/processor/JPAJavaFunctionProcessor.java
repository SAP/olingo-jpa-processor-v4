package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ENUMERATION_UNKNOWN;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaOperation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public class JPAJavaFunctionProcessor {

  private final UriResourceFunction uriResourceFunction;
  private final JPAJavaFunction jpaFunction;
  private final Object[] parameters;
  private final JPAServiceDocument sd;

  public JPAJavaFunctionProcessor(final JPAServiceDocument sd, final UriResourceFunction uriResourceFunction,
      final JPAJavaFunction jpaFunction, final Object... parameter) {
    this.uriResourceFunction = uriResourceFunction;
    this.jpaFunction = jpaFunction;
    this.parameters = parameter;
    this.sd = sd;
  }

  public Object process() throws ODataApplicationException {
    try {
      final Object instance = createInstance(parameters, jpaFunction);
      final List<Object> parameter = fillParameter(uriResourceFunction, jpaFunction);

      return jpaFunction.getMethod().invoke(instance, parameter.toArray());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof ODataApplicationException) {
        throw (ODataApplicationException) cause;
      } else {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  private Object createInstance(final Object[] parameters, final JPAJavaOperation jpaOperation)
      throws InstantiationException, IllegalAccessException, InvocationTargetException {

    final Constructor<?> c = jpaOperation.getConstructor();
    final Object[] paramValues = new Object[c.getParameters().length];
    int i = 0;
    for (final Parameter p : c.getParameters()) {
      for (final Object o : parameters) {
        if (p.getType().isAssignableFrom(o.getClass())) {
          paramValues[i] = o;
          break;
        }
      }
      i++;
    }
    return c.newInstance(paramValues);
  }

  private List<Object> fillParameter(final UriResourceFunction uriResourceFunction, final JPAJavaOperation jpaOperation)
      throws ODataJPAModelException, ODataApplicationException {

    final Parameter[] methodParameter = jpaOperation.getMethod().getParameters();
    final List<Object> parameter = new ArrayList<>();

    for (final Parameter declaredParameter : methodParameter) {
      for (final UriParameter providedParameter : uriResourceFunction.getParameters()) {
        final JPAParameter jpaParameter = jpaOperation.getParameter(declaredParameter);
        if (jpaParameter.getName().equals(providedParameter.getName())) {
          final String value = providedParameter.getText();
          if (value != null)
            parameter.add(getValue(uriResourceFunction.getFunction(), jpaParameter, value));
          else
            parameter.add(null);
          break;
        }
      }
    }
    return parameter;
  }

  private Object getValue(final EdmFunction edmFunction, final JPAParameter parameter, final String uriValue)
      throws ODataApplicationException {
    final String value = uriValue.replace("'", "");
    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      switch (edmParam.getType().getKind()) {
        case PRIMITIVE:
          return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, edmParam.getMaxLength(),
              edmParam.getPrecision(), edmParam.getScale(), true, parameter.getType());
        case ENUM:
          final JPAEnumerationAttribute enumeration = sd.getEnumType(parameter.getTypeFQN()
              .getFullQualifiedNameAsString());
          if (enumeration == null)
            throw new ODataJPAProcessorException(ENUMERATION_UNKNOWN, HttpStatusCode.BAD_REQUEST, parameter.getName());
          return enumeration.enumOf(value);
        default:
          throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
              HttpStatusCode.NOT_IMPLEMENTED, uriValue, parameter.getName());
      }

    } catch (EdmPrimitiveTypeException | ODataJPAModelException e) {
      // Unable to convert value %1$s of parameter %2$s
      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
          HttpStatusCode.NOT_IMPLEMENTED, e, uriValue, parameter.getName());
    }
  }
}
