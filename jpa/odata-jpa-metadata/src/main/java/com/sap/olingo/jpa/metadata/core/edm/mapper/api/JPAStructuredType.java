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
  public JPAAssociationAttribute getAssociation(String internalName) throws ODataJPAModelException;

  public JPAAssociationPath getAssociationPath(String externalName) throws ODataJPAModelException;

  /**
   * Searches in the navigation properties that are available for this type via the OData service. That is:
   * <ul>
   * <li> All not ignored navigation properties of this type.
   * <li> All not ignored navigation properties from super types.
   * <li> All not ignored navigation properties from embedded types.
   * </ul>
   * @return null if no navigation property found.
   * @throws ODataJPAModelException
   */
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException;

  public JPAAttribute getAttribute(String internalName) throws ODataJPAModelException;

  public List<JPAAttribute> getAttributes() throws ODataJPAModelException;

  /**
   * In case the type is within the given association path, the sub-path is returned.
   * E.g. structured type is AdministrativeInformation and associationPath = AdministrativeInformation/Created/User
   * Created/User is returned.
   * @param associationPath
   * @return
   * @throws ODataJPAModelException
   */
  public JPAAssociationPath getDeclaredAssociation(JPAAssociationPath associationPath) throws ODataJPAModelException;

  public JPAAssociationPath getDeclaredAssociation(String externalName) throws ODataJPAModelException;

  /**
   * List of all associations that are declared at this type. That is:
   * <ul>
   * <li> All not ignored navigation properties of this type.
   * <li> All not ignored navigation properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException;

  /**
   * List of all associations that are declared at this type. That is:
   * <ul>
   * <li> All not ignored properties of this type.
   * <li> All not ignored properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException;

  /**
   * List of all associations that are declared at this type. That is:
   * <ul>
   * <li> All not ignored collection properties of this type.
   * <li> All not ignored collection properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException;

  /**
   * List of all associations that are declared at this type. That is:
   * <ul>
   * <li> All not ignored collection properties of this type.
   * <li> All not ignored collection properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public JPAPath getPath(String externalName) throws ODataJPAModelException;

  /**
   * List of all attributes that are available for this type via the OData service. That is:
   * <ul>
   * <li> All not ignored properties of the type.
   * <li> All not ignored properties from super types.
   * <li> All not ignored properties from embedded types.
   * </ul>
   * @return List of all attributes that are available via the OData service.
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getPathList() throws ODataJPAModelException;

  public Class<?> getTypeClass();

  public boolean isAbstract();

}
