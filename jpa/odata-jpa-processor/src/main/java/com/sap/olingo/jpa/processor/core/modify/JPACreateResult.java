package com.sap.olingo.jpa.processor.core.modify;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;
import com.sap.org.jpa.processor.core.converter.JPAExpandResult;

abstract class JPACreateResult implements JPAExpandResult {

  protected final JPAEntityType et;
  protected final Map<JPAAssociationPath, JPAExpandResult> children;
  protected final List<JPAPath> pathList;
  protected final Locale locale;
  protected final JPAConversionHelper helper;
  protected final Map<String, List<String>> requestHeaders;

  public JPACreateResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException, ODataJPAProcessorException {

    this.et = et;
    this.helper = new JPAConversionHelper();
    this.children = new HashMap<JPAAssociationPath, JPAExpandResult>(0);
    this.pathList = et.getPathList();
    this.locale = ExpressionUtil.determineLocale(requestHeaders);
    this.requestHeaders = requestHeaders;
  }

  @Override
  public Map<JPAAssociationPath, JPAExpandResult> getChildren() {
    return children;
  }

  @Override
  public Integer getCount() {
    return null;
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public boolean hasCount() {
    return false;
  }

  protected void addValueToTuple(final JPATuple tuple, final JPAPath path, final int index, Object value)
      throws ODataJPAProcessorException {
    if (path.getPath().get(index) instanceof JPADescriptionAttribute) {
      @SuppressWarnings("unchecked")
      Collection<Object> desc = (Collection<Object>) value;
      if (desc != null) {

        for (Object entry : desc) {
          final Map<String, Object> descGetterMap = helper.buildGetterMap(entry);
          JPADescriptionAttribute jpaAttribute = (JPADescriptionAttribute) path.getPath().get(index);
          value = descGetterMap.get(jpaAttribute.getLocaleFieldName().getPath().get(0).getInternalName());
          if (locale.getLanguage().equals(value)
              || locale.toString().equals(value)) {
            tuple.addElement(path.getAlias(), path.getLeaf().getType(), value);
            break;
          }
        }
      } else
        tuple.addElement(path.getAlias(), path.getLeaf().getType(), null);
    } else {
      tuple.addElement(path.getAlias(), path.getLeaf().getType(), value);
    }
  }

}