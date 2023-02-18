package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.CheckForNull;

public interface JPAAnnotatable {

  /**
   * Converts a path given as a string of internal (Java) attribute names into a JPAPath.
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  ODataPropertyPath convertStringToPath(final String internalPath) throws ODataPathNotFoundException;

  /**
   *
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  ODataNavigationPath convertStringToNavigationPath(final String internalPath) throws ODataPathNotFoundException;

  /**
   * Searches to java annotation at an OData annotatable element. In case no annotation is found, Null is returned;
   * @param name of the annotation, as it would be returned by {@link Class#getName()}
   * @return
   */
  @CheckForNull
  Annotation javaAnnotation(String name);

  /**
   * Provide all java annotation at the annotatable that come from the given package.
   * @param packageName
   * @return A map with key simple name ( {@link Class#getSimpleName()}) of the annotation and value the annotation
   * instance
   */
  Map<String, Annotation> javaAnnotations(String packageName);

}
