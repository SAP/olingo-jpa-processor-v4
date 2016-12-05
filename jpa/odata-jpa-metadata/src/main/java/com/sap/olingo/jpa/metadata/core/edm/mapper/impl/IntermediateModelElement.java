package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess;

abstract class IntermediateModelElement implements IntermediateModelItemAccess {

  protected static JPAEdmMetadataPostProcessor postProcessor = new DefaultEdmPostProcessor();
  protected static final JPANameBuilder IntNameBuilder = new JPANameBuilder();
  protected final JPAEdmNameBuilder nameBuilder;
  protected final String internalName;
  final protected List<CsdlAnnotation> edmAnnotations;
  private boolean toBeIgnored;
  private String externalName;

  static void setPostProcessor(final JPAEdmMetadataPostProcessor pP) {
    postProcessor = pP;
  }

  public IntermediateModelElement(final JPAEdmNameBuilder nameBuilder, final String internalName) {
    super();
    this.nameBuilder = nameBuilder;
    this.internalName = internalName;
    this.edmAnnotations = new ArrayList<CsdlAnnotation>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#getExternalName()
   */
  @Override
  public String getExternalName() {
    return externalName;
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return nameBuilder.buildFQN(getExternalName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#getInternalName()
   */
  @Override
  public String getInternalName() {
    return internalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#ignore()
   */
  @Override
  public boolean ignore() {
    return toBeIgnored;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#setExternalName(java.lang.String)
   */
  @Override
  public void setExternalName(final String externalName) {
    this.externalName = externalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#setIgnore(boolean)
   */
  @Override
  public void setIgnore(final boolean ignore) {
    this.toBeIgnored = ignore;
  }

  protected abstract void lazyBuildEdmItem() throws ODataJPAModelException;

  @SuppressWarnings("unchecked")
  protected <T> List<?> extractEdmModelElements(final Map<String, ?> mappingBuffer) throws ODataJPAModelException {
    final List<T> extractionTarget = new ArrayList<T>();
    for (final String externalName : mappingBuffer.keySet()) {
      if (!((IntermediateModelElement) mappingBuffer.get(externalName)).toBeIgnored) {
        final IntermediateModelElement func = (IntermediateModelElement) mappingBuffer.get(externalName);
        final CsdlAbstractEdmItem edmFunc = func.getEdmItem();
        if (!func.ignore())
          extractionTarget.add((T) edmFunc);
      }
    }
    return returnNullIfEmpty(extractionTarget);
  }

  protected IntermediateModelElement findModelElementByEdmItem(final String edmEntityItemName,
      final Map<String, ?> buffer) throws ODataJPAModelException {
    for (final String internalName : buffer.keySet()) {
      final IntermediateModelElement modelElement = (IntermediateModelElement) buffer.get(internalName);
      if (edmEntityItemName.equals(modelElement.getExternalName())) {
        return modelElement;
      }
    }
    return null;

  }

  protected <T> List<T> returnNullIfEmpty(final List<T> list) {
    return list == null || list.isEmpty() ? null : list;
  }

  abstract CsdlAbstractEdmItem getEdmItem() throws ODataJPAModelException;

  /**
   * Convert annotations at an annotatable element into OData annotations
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation}
   * 
   * @throws ODataJPAModelException
   */
  protected void getAnnotations(List<CsdlAnnotation> edmAnnotations, Member member, String internalName) throws ODataJPAModelException {
    if (member instanceof AnnotatedElement) {
      final EdmAnnotation jpaAnnotation = ((AnnotatedElement) member)
          .getAnnotation(EdmAnnotation.class);
  
      if (jpaAnnotation != null) {
        CsdlAnnotation edmAnnotation = new CsdlAnnotation();
        edmAnnotation.setTerm(jpaAnnotation.term());
        edmAnnotation.setQualifier(jpaAnnotation.qualifier());
        if (!(jpaAnnotation.constantExpression().type() == ConstantExpressionType.Int
            && jpaAnnotation.constantExpression().value().equals("default"))
            && !(jpaAnnotation.dynamicExpression().path().isEmpty())) {
          throw new ODataJPAModelException(
              ODataJPAModelException.MessageKeys.ODATA_ANNOTATION_TWO_EXPRESSIONS, internalName);
        } else if (jpaAnnotation.constantExpression() != null) {
          edmAnnotation.setExpression(new CsdlConstantExpression(jpaAnnotation.constantExpression().type(),
              jpaAnnotation.constantExpression().value()));
        }
        edmAnnotations.add(edmAnnotation);
      }
    }
  }
}
