package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.Objects;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlOperation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class ODataActionKey {

  private final String externalName;
  private final FullQualifiedName bindingParameterType;

  public ODataActionKey(final IntermediateOperation operation) throws ODataJPAModelException {
    this.externalName = operation.getExternalName();
    this.bindingParameterType = determineBindingParameter(operation);
  }

  public ODataActionKey(final String externalName, final FullQualifiedName actionFqn) {
    this.externalName = externalName;
    this.bindingParameterType = actionFqn;
  }

  private FullQualifiedName determineBindingParameter(final IntermediateOperation operation)
      throws ODataJPAModelException {
    if (operation.isBound()) {
      final CsdlParameter parameter = ((CsdlOperation) operation.getEdmItem()).getParameters().get(0);
      return parameter.getTypeFQN();
    }
    return null;
  }

  public String getExternalName() {
    return externalName;
  }

  public FullQualifiedName getBindingParameterType() {
    return bindingParameterType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(externalName, bindingParameterType);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ODataActionKey)) return false;
    final ODataActionKey other = (ODataActionKey) obj;
    return Objects.equals(bindingParameterType, other.bindingParameterType) && Objects.equals(externalName,
        other.externalName);
  }

  @Override
  public String toString() {
    return "ODataActionKey [externalName=" + externalName + ", bindingParameterType=" + bindingParameterType + "]";
  }
}
