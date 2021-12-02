package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * External view on an Intermediate Structured Type.
 *
 * @author Oliver Grande
 *
 */
public interface JPAStructuredType extends JPAElement {

  public JPAAssociationAttribute getAssociation(@Nonnull final String internalName) throws ODataJPAModelException;

  /**
   * Searches for an AssociationPath defined by the name used in the OData metadata in all the navigation properties
   * that are available for this type via the OData service. That is:
   * <ul>
   * <li> All not ignored navigation properties of this type.
   * <li> All not ignored navigation properties from super types.
   * <li> All not ignored navigation properties from embedded types.
   * </ul>
   * @param externalName
   * @return
   * @throws ODataJPAModelException
   */
  public JPAAssociationPath getAssociationPath(@Nonnull final String externalName) throws ODataJPAModelException;
  
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

  /**
   * Returns declared attribute. Attributes that shall be ignored are ignored.<p>
   * In case all properties are needed use {@link #getDeclaredAttribute(String)}
   * @param internalName
   * @return
   * @throws ODataJPAModelException
   */
  public Optional<JPAAttribute> getAttribute(@Nonnull final String internalName) throws ODataJPAModelException;

  public Optional<JPAAttribute> getAttribute(@Nonnull final UriResourceProperty uriResourceItem)
      throws ODataJPAModelException;

  @Nonnull
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException;

  /**
   * List of the path to all collection properties of this type. That is:
   * <ul>
   * <li> All not ignored collection properties of this type.
   * <li> All not ignored collection properties from super types.
   * <li> All not ignored collection properties from embedded types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  @Nonnull
  public List<JPAPath> getCollectionAttributesPath() throws ODataJPAModelException;

  /**
   * List of all associations that are declared at this type. That is:
   * <ul>
   * <li> All navigation properties of this type.
   * <li> All navigation properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  @Nonnull
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException;

  /**
   * List of all attributes that are declared at this type. That is:
   * <ul>
   * <li> All properties of this type.
   * <li> All properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  @Nonnull
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException;

  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException;

  /**
   * List of all collection attributes that are declared at this type. That is:
   * <ul>
   * <li> All collection properties of this type.
   * <li> All collection properties from super types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException;

  public JPAPath getPath(final String externalName) throws ODataJPAModelException;

  public JPAPath getPath(final String externalName, final boolean respectIgnore) throws ODataJPAModelException;

  /**
   * List of the path to all collection properties of this type. That is:
   * <ul>
   * <li> All not ignored properties of the type.
   * <li> All not ignored properties from super types.
   * <li> All not ignored properties from embedded types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getPathList() throws ODataJPAModelException;

  /**
   * List of all protected Attributes including protection/claim information. That is:
   * <ul>
   * <li> All not ignored protected properties of the type.
   * <li> All not ignored protected properties from super types.
   * <li> All not ignored protected properties from embedded types.
   * </ul>
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAProtectionInfo> getProtections() throws ODataJPAModelException;

  public Class<?> getTypeClass();

  public boolean isAbstract();
}
