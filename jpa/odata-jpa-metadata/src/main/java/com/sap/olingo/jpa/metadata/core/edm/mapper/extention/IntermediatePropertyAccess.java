package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Override generated metadata for a property.
 * @author Oliver Grande
 *
 */
public interface IntermediatePropertyAccess extends IntermediateModelItemAccess {
  public boolean isEtag();

  /**
   * Enables to add annotations to a property, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or should changed be during
   * runtime
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);

  public boolean hasProtection();

  /**
   * Returns a list of names of the claims that shall be matched with this property
   * @return
   */
  public Set<String> getProtectionClaimNames();

  /**
   * Provides a List of path to the protected attributed
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getProtectionPath(String claimName) throws ODataJPAModelException;
}
