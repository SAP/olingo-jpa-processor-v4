package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;
import java.util.Optional;

import javax.annotation.CheckForNull;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEntityType extends JPAStructuredType, JPAAnnotatable {
  /**
   * Searches for a Collection Property defined by the name used in the OData metadata in all the collection properties
   * that are available for this type via the OData service. That is:
   * <ul>
   * <li>All not ignored collection properties of this type.
   * <li>All not ignored collection properties from super types.
   * <li>All not ignored collection properties from embedded types.
   * </ul>
   * @param externalName
   * @return
   * @throws ODataJPAModelException
   */
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException;

  /**
   *
   * @return Mime type of streaming content
   * @throws ODataJPAModelException
   */
  public String getContentType() throws ODataJPAModelException;

  public JPAPath getContentTypeAttributePath() throws ODataJPAModelException;

  public JPAPath getEtagPath() throws ODataJPAModelException;

  /**
   * Returns a resolved list of all attributes that are marked as Id, so the attributes of an EmbeddedId are returned as
   * separate entries. They are returned in the same order they are mentioned in the corresponding type.
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAAttribute> getKey() throws ODataJPAModelException;

  /**
   * Returns a list of path of all attributes annotated as Id. EmbeddedId are <b>not</b> resolved
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getKeyPath() throws ODataJPAModelException;

  /**
   * Returns the class of the Key. This could by either a primitive type, the IdClass or the Embeddable of an EmbeddedId
   * @return
   */
  public Class<?> getKeyType();

  /**
   * True in case the entity type has a compound key, so an EmbeddedId or multiple id properties
   * @return
   */
  public boolean hasCompoundKey();

  /**
   * True in case the entity type has an EmbeddedId
   * @return
   */
  public boolean hasEmbeddedKey();

  /**
   * @return a list of JPAPath to attributes marked with EdmSearchable
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getSearchablePath() throws ODataJPAModelException;

  public JPAPath getStreamAttributePath() throws ODataJPAModelException;

  /**
   *
   * @return Name of the database table. The table name is composed from schema name and table name
   */
  public String getTableName();

  public boolean hasEtag() throws ODataJPAModelException;

  @CheckForNull
  public JPAEtagValidator getEtagValidator() throws ODataJPAModelException;

  public boolean hasStream() throws ODataJPAModelException;

  public <X extends EdmQueryExtensionProvider> Optional<JPAQueryExtension<X>> getQueryExtension()
      throws ODataJPAModelException;
}
