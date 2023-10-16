package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateModelItemAccess;

abstract class IntermediateModelElement implements IntermediateModelItemAccess {

  protected static JPAEdmMetadataPostProcessor postProcessor = new DefaultEdmPostProcessor();
  protected static final JPANameBuilder InternalNameBuilder = new JPANameBuilder();
  protected final JPAEdmNameBuilder nameBuilder;
  protected final String internalName;
  protected final List<CsdlAnnotation> edmAnnotations;
  private final IntermediateAnnotationInformation annotationInformation;
  private boolean toBeIgnored;
  private String externalName;

  static void setPostProcessor(final JPAEdmMetadataPostProcessor processor) {
    postProcessor = processor;
  }

  IntermediateModelElement(final JPAEdmNameBuilder nameBuilder, final String internalName,
      final IntermediateAnnotationInformation annotationInformation) {
    super();
    this.nameBuilder = nameBuilder;
    this.internalName = internalName;
    this.edmAnnotations = new ArrayList<>();
    this.annotationInformation = annotationInformation;
  }

  @Override
  public String getExternalName() {
    return externalName;
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return buildFQN(getExternalName());
  }

  @Override
  public String getInternalName() {
    return internalName;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateModelItemAccess#ignore()
   */
  @Override
  public boolean ignore() {
    return toBeIgnored;
  }

  protected void setExternalName(final String externalName) {
    this.externalName = externalName;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateModelItemAccess#setIgnore(boolean)
   */
  @Override
  public void setIgnore(final boolean ignore) {
    this.toBeIgnored = ignore;
  }

  protected abstract void lazyBuildEdmItem() throws ODataJPAModelException;

  @SuppressWarnings("unchecked")
  protected <T extends CsdlAbstractEdmItem> List<T> extractEdmModelElements(
      final Map<?, ? extends IntermediateModelElement> mappingBuffer) throws ODataJPAModelException {
    final List<T> extractionTarget = new ArrayList<>();

    for (final IntermediateModelElement bufferItem : mappingBuffer.values()) {

      if (!bufferItem.toBeIgnored) { // NOSONAR
        final IntermediateModelElement element = bufferItem;
        final CsdlAbstractEdmItem edmItem = element.getEdmItem();
        if (!element.ignore())
          extractionTarget.add((T) edmItem);
      }
    }
    return extractionTarget;
  }

  protected <T extends IntermediateModelElement> IntermediateModelElement findModelElementByEdmItem(
      final String edmEntityItemName, final Map<String, T> buffer) {

    for (final T bufferItem : buffer.values()) {
      if (edmEntityItemName.equals(bufferItem.getExternalName())) {
        return bufferItem;
      }
    }
    return null;
  }

  protected <T> List<T> returnNullIfEmpty(final List<T> list) {
    return list == null ? Collections.emptyList() : list;
  }

  protected String returnNullIfEmpty(final String value) {
    return value == null || value.isEmpty() ? null : value;
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
  protected void getAnnotations(final List<CsdlAnnotation> edmAnnotations, final Member member,
      final String internalName) throws ODataJPAModelException {
    if (member instanceof final AnnotatedElement annotatedElement) {
      extractAnnotations(edmAnnotations, annotatedElement, internalName);
    }
  }

  protected void getAnnotations(final List<CsdlAnnotation> edmAnnotations, final Class<?> clazz,
      final String internalName) throws ODataJPAModelException {
    if (clazz instanceof AnnotatedElement) {
      extractAnnotations(edmAnnotations, clazz, internalName);
    }
  }

  /**
   * @param t
   * @return
   */
  protected final String buildFQTableName(final String schema, final String name) {
    final StringBuilder fqt = new StringBuilder();
    if (schema != null && !schema.isEmpty()) {
      fqt.append(schema);
      fqt.append(".");
    }
    fqt.append(name);
    return fqt.toString();
  }

  private void extractAnnotations(final List<CsdlAnnotation> edmAnnotations, final AnnotatedElement element,
      final String internalName)
      throws ODataJPAModelException {
    final EdmAnnotation jpaAnnotation = element.getAnnotation(EdmAnnotation.class);

    if (jpaAnnotation != null) {
      final CsdlAnnotation edmAnnotation = new CsdlAnnotation();
      final String qualifier = jpaAnnotation.qualifier();
      edmAnnotation.setTerm(jpaAnnotation.term());
      edmAnnotation.setQualifier(qualifier.isEmpty() ? null : qualifier);
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
  protected Class<?> boxPrimitive(final Class<?> javaType) {// NOSONAR

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

  /**
   *
   * @param name
   * @return
   */
  protected final FullQualifiedName buildFQN(final String name) {
    return new FullQualifiedName(nameBuilder.getNamespace(), name);
  }

  @Override
  public String toString() {
    return "IntermediateModelElement [internalName=" + internalName + ", externalName="
        + externalName + ", toBeIgnored=" + toBeIgnored + "]";
  }

  /**
   * @param value
   * @return true if string value is null or empty
   */
  protected final boolean emptyString(final String value) {
    return value == null || value.isEmpty();
  }

  protected IntermediateAnnotationInformation getAnnotationInformation() {
    return annotationInformation;
  }

  protected CsdlAnnotation filterAnnotation(final String alias, final String term) {
    final String annotationFqn = annotationInformation.getReferences().convertAlias(alias) + "." + term;
    return edmAnnotations.stream()
        .filter(a -> annotationFqn.equals(a.getTerm()))
        .findFirst()
        .orElse(null);
  }

  protected void retrieveAnnotations(final ODataAnnotatable annotatable, final Applicability applicability) {
    for (final AnnotationProvider provider : annotationInformation.getAnnotationProvider())
      edmAnnotations.addAll(provider.getAnnotations(applicability, annotatable, annotationInformation.getReferences()));
  }

  protected Map<String, Annotation> findJavaAnnotation(final String packageName, final Class<?> clazz) {
    return findJavaAnnotation(packageName, clazz.getAnnotations());
  }

  protected Map<String, Annotation> findJavaAnnotation(final String packageName,
      final AnnotatedElement annotatedElement) {
    return findJavaAnnotation(packageName, annotatedElement.getAnnotations());
  }

  private Map<String, Annotation> findJavaAnnotation(final String packageName, final Annotation[] annotations) {
    final Map<String, Annotation> result = new HashMap<>();
    for (final Annotation a : annotations) {
      if (a.annotationType().getPackage().getName().equals(packageName)) {
        result.put(a.annotationType().getSimpleName(), a);
      }
    }
    return result;
  }

}
