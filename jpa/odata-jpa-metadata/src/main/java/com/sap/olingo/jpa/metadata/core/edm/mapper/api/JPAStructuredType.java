package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * External view on an Intermediate Structured Type.
 *
 * @author Oliver Grande
 *
 */
public interface JPAStructuredType extends JPAElement {
  /**
   * Searches in the navigation properties that are available for this type via the OData service. That is:
   * <ul>
   * <li> All not ignored navigation properties of this type.
   * <li> All not ignored navigation properties from supertypes are included
   * <li> All not ignored navigation properties from embedded types are included.
   * </ul>
   * @return null if no navigation property found.
   * @throws ODataJPAModelException
   */
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException;

  public JPAAssociationAttribute getAssociation(String internalName) throws ODataJPAModelException;

  public JPAAssociationPath getAssociationPath(String externalName) throws ODataJPAModelException;

  public JPAAssociationPath getDeclaredAssociation(String externalName) throws ODataJPAModelException;

  public JPAAttribute getAttribute(String internalName) throws ODataJPAModelException;

  public List<JPAAttribute> getAttributes() throws ODataJPAModelException;

  public JPAPath getPath(String externalName) throws ODataJPAModelException;

  /**
   * List of all attributes that are available for this type via the OData service. That is:
   * <ul>
   * <li> All not ignored properties of the type.
   * <li> All not ignored properties from supertypes.
   * <li> All not ignored properties from embedded types.
   * </ul>
   * @return List of all attributes that are available via the OData service.
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getPathList() throws ODataJPAModelException;

  public Class<?> getTypeClass();

  /**
   * In case the type is within the given association path, the sub-path is returned.
   * E.g. structured type is AdministrativeInformation and associationPath = AdministrativeInformation/Created/User
   * Created/User is returned.
   * @param associationPath
   * @return
   * @throws ODataJPAModelException
   */
  public JPAAssociationPath getDeclaredAssociation(JPAAssociationPath associationPath) throws ODataJPAModelException;

}
