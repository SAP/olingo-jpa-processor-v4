package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess;

abstract class IntermediateModelElement implements IntermediateModelItemAccess {

  protected static JPAEdmMetadataPostProcessor postProcessor = new DefaultEdmPostProcessor();
  protected static final JPANameBuilder IntNameBuilder = new JPANameBuilder();
  protected final JPAEdmNameBuilder nameBuilder;
  protected final String internalName;
  protected final List<CsdlAnnotation> edmAnnotations;
  private boolean toBeIgnored;
  private String externalName;

  static void setPostProcessor(final JPAEdmMetadataPostProcessor pP) {
    postProcessor = pP;
  }

  public IntermediateModelElement(final JPAEdmNameBuilder nameBuilder, final String internalName) {
    super();
    this.nameBuilder = nameBuilder;
    this.internalName = internalName;
    this.edmAnnotations = new ArrayList<>();
  }

  @Override
  public String getExternalName() {
    return externalName;
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return nameBuilder.buildFQN(getExternalName());
  }

  @Override
  public String getInternalName() {
    return internalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess#ignore()
   */
  @Override
  public boolean ignore() {
    return toBeIgnored;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess#setExternalName(java.lang.String)
   */
  @Override
  public void setExternalName(final String externalName) {
    this.externalName = externalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess#setIgnore(boolean)
   */
  @Override
  public void setIgnore(final boolean ignore) {
    this.toBeIgnored = ignore;
  }

  protected abstract void lazyBuildEdmItem() throws ODataJPAModelException;

  @SuppressWarnings("unchecked")
  protected <T> List<?> extractEdmModelElements(final Map<String, ? extends IntermediateModelElement> mappingBuffer)
      throws ODataJPAModelException {
    final List<T> extractionTarget = new ArrayList<>();

    for (final Entry<String, ? extends IntermediateModelElement> bufferItem : mappingBuffer.entrySet()) {

      if (!((IntermediateModelElement) bufferItem.getValue()).toBeIgnored) { // NOSONAR
        final IntermediateModelElement element = bufferItem.getValue();
        final CsdlAbstractEdmItem edmItem = element.getEdmItem();
        if (!element.ignore())
          extractionTarget.add((T) edmItem);
      }
    }
    return extractionTarget;
  }

  protected IntermediateModelElement findModelElementByEdmItem(final String edmEntityItemName,
      final Map<String, ? extends IntermediateModelElement> buffer) {

    for (final Entry<String, ?> bufferItem : buffer.entrySet()) {
      final IntermediateModelElement modelElement = (IntermediateModelElement) bufferItem.getValue();

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
   * @param edmAnnotations
   * @param member
   * @param internalName
   * @param property
   * @throws ODataJPAModelException
   */
  protected void getAnnotations(List<CsdlAnnotation> edmAnnotations, Member member, String internalName,
      AppliesTo property) throws ODataJPAModelException {
    if (member instanceof AnnotatedElement) {
      extractAnnotations(edmAnnotations, (AnnotatedElement) member, internalName);
    }
  }

  protected void getAnnotations(List<CsdlAnnotation> edmAnnotations, Class<?> clazz, String internalName)
      throws ODataJPAModelException {
    if (clazz instanceof AnnotatedElement) {
      extractAnnotations(edmAnnotations, clazz, internalName);
    }
  }

  private void extractAnnotations(List<CsdlAnnotation> edmAnnotations, AnnotatedElement element, String internalName)
      throws ODataJPAModelException {
    final EdmAnnotation jpaAnnotation = element.getAnnotation(EdmAnnotation.class);

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

  /**
   * https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html
   * @param javaType
   * 
   * @return
   */
  protected Class<?> boxPrimitive(Class<?> javaType) {// NOSONAR

    if (javaType == int.class || javaType == Integer.class)
      return Integer.class;
    else if (javaType == long.class || javaType == Long.class)
      return Long.class;
    else if (javaType == boolean.class || javaType == Boolean.class)
      return Boolean.class;
    else if (javaType == byte.class || javaType == Byte.class)
      return Byte.class;
    else if (javaType == char.class || javaType == Character.class)
      return Character.class;
    else if (javaType == float.class || javaType == Float.class)
      return Float.class;
    else if (javaType == short.class || javaType == Short.class)
      return Short.class;
    else if (javaType == double.class || javaType == Double.class)
      return Double.class;

    return null;
  }

  @Override
  public String toString() {
    return "IntermediateModelElement [internalName=" + internalName + ", externalName="
        + externalName + ", toBeIgnored=" + toBeIgnored + "]";
  }
}
