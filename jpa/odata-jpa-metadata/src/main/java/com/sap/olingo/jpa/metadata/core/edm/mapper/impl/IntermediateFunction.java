package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.FUNC_RETURN_TYPE_ENTITY_SET;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.FUNC_UNBOUND_ENTITY_SET;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Mapper, that is able to convert different metadata resources into a edm function metadata. It is important to know
 * that:
 * <cite>Functions MUST NOT have observable side effects and MUST return a single instance or a collection of instances
 * of any type.</cite>
 * <p>For details about Function metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398010"
 * >OData Version 4.0 Part 3 - 12.2 Element edm:Function</a>
 * @author Oliver Grande
 *
 */

abstract class IntermediateFunction extends IntermediateOperation implements JPAFunction {
  protected CsdlFunction edmFunction;
  protected final EdmFunction jpaFunction;
  protected final IntermediateSchema schema;

  IntermediateFunction(final JPAEdmNameBuilder nameBuilder, final EdmFunction jpaFunction,
      final IntermediateSchema schema, final String internalName) {

    super(nameBuilder, internalName);
    this.jpaFunction = jpaFunction;
    this.schema = schema;
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmFunction == null) {
      edmFunction = new CsdlFunction();
      edmFunction.setName(getExternalName());
      edmFunction.setParameters(returnNullIfEmpty(determineEdmInputParameter()));
      edmFunction.setReturnType(determineEdmResultType(jpaFunction.returnType()));
      edmFunction.setBound(jpaFunction.isBound());
      // TODO edmFunction.setComposable(isComposable)
      edmFunction.setComposable(false);
      /*
       * Bound actions and functions that return an entity or a collection of entities MAY specify an entity set path if
       * the entity set of the returned entities depends on the entity set of the binding parameter value.
       * The entity set path consists of a series of segments joined together with forward slashes.
       * The first segment of the entity set path MUST be the name of the binding parameter. The remaining segments of
       * the entity set path MUST represent navigation segments or type casts.
       * A navigation segment names the simple identifier of the navigation property to be traversed. A type-cast
       * segment names the qualified name of the entity type that should be returned from the type cast.
       */
      edmFunction.setEntitySetPath(setEntitySetPath());

    }
  }

  private String setEntitySetPath() throws ODataJPAModelException {

    final String path = jpaFunction.entitySetPath();
    if (path == null || path.isEmpty())
      return null;
    if (!jpaFunction.isBound())
      // Entity Set Path shall only provided for bound functions. Function '%1$s' is unbound.
      throw new ODataJPAModelException(FUNC_UNBOUND_ENTITY_SET, jpaFunction.functionName());
    if (schema.getEntityType(jpaFunction.returnType().type()) == null)
      // Entity Set Path shall only if a function returns an entity or collection of entities. Function '%1$s' has a
      // wrong return type.
      throw new ODataJPAModelException(FUNC_RETURN_TYPE_ENTITY_SET, jpaFunction.functionName());
    return path;
  }

  @Override
  CsdlFunction getEdmItem() throws ODataJPAModelException {
    if (edmFunction == null) {
      lazyBuildEdmItem();
    }
    return edmFunction;
  }

  String getUserDefinedFunction() {
    return jpaFunction.functionName();
  }

  @Override
  boolean hasImport() {
    return jpaFunction.hasFunctionImport();
  }

  @Override
  public boolean isBound() throws ODataJPAModelException {
    return getEdmItem().isBound();
  }

  protected abstract List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException;

  protected abstract CsdlReturnType determineEdmResultType(final ReturnType returnType) throws ODataJPAModelException;

  protected abstract FullQualifiedName determineParameterType(final Class<?> type,
      final EdmParameter definedParameter) throws ODataJPAModelException;

  protected class IntermediateFunctionParameter implements JPAParameter {
    private final EdmParameter jpaParameter;
    private final String internalName;
    private final String externalName;
    private final Class<?> type;

    IntermediateFunctionParameter(final EdmParameter jpaParameter) {
      this.jpaParameter = jpaParameter;
      this.internalName = jpaParameter.parameterName();
      this.externalName = jpaParameter.name();
      this.type = jpaParameter.type();
    }

    IntermediateFunctionParameter(final EdmParameter jpaParameter, final String externalName,
        final String internalName, final Class<?> type) {
      this.jpaParameter = jpaParameter;
      this.internalName = internalName;
      this.externalName = externalName;
      this.type = type;
    }

    @Override
    public String getInternalName() {
      return internalName;
    }

    @Override
    public String getName() {
      return externalName;
    }

    @Override
    public Class<?> getType() {
      return type.isPrimitive() ? boxPrimitive(type) : type;
    }

    @Override
    public Integer getMaxLength() {
      return jpaParameter.maxLength();
    }

    @Override
    public Integer getPrecision() {
      return jpaParameter.precision();
    }

    @Override
    public Integer getScale() {
      return jpaParameter.scale();
    }

    @Override
    public FullQualifiedName getTypeFQN() throws ODataJPAModelException {
      return determineParameterType(type, jpaParameter);
    }

    @Override
    public SRID getSrid() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
