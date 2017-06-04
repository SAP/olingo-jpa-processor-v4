package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class IntermediateEnumerationType extends IntermediateModelElement {

  private CsdlEnumType edmEnumType;
  private Class<?> javaEnum;

  public IntermediateEnumerationType(JPAEdmNameBuilder nameBuilder, Class<? extends ODataEnum> javaEnum) {
    super(nameBuilder, javaEnum.getSimpleName());
    assert javaEnum.isEnum();
    this.javaEnum = javaEnum;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEnumType == null) {
      edmEnumType = new CsdlEnumType();
//      edmEnumType.setFlags(isFlags);
      edmEnumType.setMembers(buildMembers());
//      edmEnumType.setName(name);
//      edmEnumType.setUnderlyingType(underlyingType);
    }
  }

  @Override
  CsdlEnumType getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmEnumType;
  }

  private List<CsdlEnumMember> buildMembers() {
    List<CsdlEnumMember> members = new ArrayList<CsdlEnumMember>();

    final List<?> javaMembers = Arrays.asList(javaEnum.getEnumConstants());

    for (final Object o : javaMembers) {
      if (o instanceof Enum) {
        final Enum<?> e = (Enum<?>) o;
        CsdlEnumMember member = new CsdlEnumMember();

        e.name();
        member.setName(e.name());
        member.setValue(Integer.valueOf(((ODataEnum) e).getValue() == null ? e.ordinal() : ((ODataEnum) e).getValue())
            .toString());
      }
    }
    return members;
  }

}
