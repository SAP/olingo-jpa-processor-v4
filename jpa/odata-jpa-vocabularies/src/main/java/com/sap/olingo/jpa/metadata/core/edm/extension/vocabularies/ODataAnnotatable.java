package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.CheckForNull;

public interface ODataAnnotatable {

  /**
   * Converts a path given as a string of internal (Java) attribute names into a JPAPath.
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  default ODataPropertyPath convertStringToPath(final String internalPath) throws ODataPathNotFoundException {
    return null;
  }

  /**
   *
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  default ODataNavigationPath convertStringToNavigationPath(final String internalPath)
      throws ODataPathNotFoundException {
    return null;
  }

  /**
   * Searches for a java annotation at an OData annotatable element. In case no annotation is found, Null is returned;
   * @param name of the annotation, as it would be returned by {@link Class#getName()}
   * @deprecated Make use of {@link #javaAnnotations(String)} instead
   * @return
   */
  @Deprecated(forRemoval = true, since = "2.1.0")
  @CheckForNull
  default Annotation javaAnnotation(final String name) {
    return null;
  }

  /**
   * Provide all java annotation at the annotatable that come from the given package.
   * @param packageName
   * @return A map with <i>key</i> simple name {@link Class#getSimpleName()}) of the annotation and <i>value</i> the
   * annotation
   * instance
   */
  Map<String, Annotation> javaAnnotations(String packageName);

}
