package com.sap.olingo.jpa.metadata.core.edm.mapper.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class MemberDouble implements Member, AnnotatedElement {
  private final Member member;
  private final AnnotatedElement annotatedElement;

  private final Map<Class<?>, Annotation> annotations;

  public MemberDouble(Member member) {
    super();
    this.member = member;
    this.annotatedElement = (AnnotatedElement) member;
    this.annotations = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    if (annotations.containsKey(annotationClass))
      return (T) annotations.get(annotationClass);
    return annotatedElement.getAnnotation(annotationClass);
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotatedElement.getAnnotations();
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return annotatedElement.getDeclaredAnnotations();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return member.getDeclaringClass();
  }

  @Override
  public String getName() {
    return member.getName();
  }

  @Override
  public int getModifiers() {
    return member.getModifiers();
  }

  @Override
  public boolean isSynthetic() {
    return member.isSynthetic();
  }

  public <T extends Annotation> void addAnnotation(Class<T> clazz, T annotation) {
    annotations.put(clazz, annotation);
  }
}
