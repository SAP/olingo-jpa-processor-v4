package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.etag.CustomETagSupport;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAServiceDocument extends CustomETagSupport {

  CsdlEntityContainer getEdmEntityContainer() throws ODataJPAModelException;

  List<CsdlSchema> getEdmSchemas() throws ODataJPAModelException;

  List<CsdlSchema> getAllSchemas() throws ODataJPAModelException;

  /**
   * Returns the internal representation of an entity type by Olingo entity type
   * @param edmType Olingo entity type
   * @return null if not found
   * @throws ODataJPAModelException
   */
  @CheckForNull
  JPAEntityType getEntity(final EdmType edmType) throws ODataJPAModelException;

  /**
   * Returns the internal representation of an entity type by given full qualified name
   * @param typeName fill qualified name of an entity type
   * @return null if not found
   */
  @CheckForNull
  JPAEntityType getEntity(final FullQualifiedName typeName);

  /**
   *
   * Returns the internal representation of an entity type by given entity set or singleton name. Entity types that are
   * annotated with EdmIgnore are ignored.
   * @param edmTargetName
   * @return null if not found
   * @throws ODataJPAModelException
   */
  @CheckForNull
  JPAEntityType getEntity(final String edmTargetName) throws ODataJPAModelException;

  /**
   *
   * Returns the internal representation of an entity type by JPA POJO class. Entity types that are annotated with
   * EdmIgnore are respected.
   * @param entityClass
   * @return null if not found
   * @throws ODataJPAModelException
   */
  @CheckForNull
  JPAEntityType getEntity(Class<?> entityClass) throws ODataJPAModelException;

  @CheckForNull
  JPAFunction getFunction(final EdmFunction function);

  /**
   * Find an Action. As the selection for overloaded actions, as described in
   * <a href="https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#_Toc31359015">
   * Action Overload Resolution (part1 protocol 11.5.5.2)</a> is done by Olingo, a resolution is not performed.
   *
   * @param action
   * @return null if action not found
   */
  @CheckForNull
  JPAAction getAction(final EdmAction action);

  @CheckForNull
  JPAEntitySet getEntitySet(final JPAEntityType entityType) throws ODataJPAModelException;

  /**
   * Find an entity set based on the external name. Entity sets marked as with EdmIgnore are ignored
   * @param edmTargetName
   * @return
   * @throws ODataJPAModelException
   */
  Optional<JPAEntitySet> getEntitySet(@Nonnull final String edmTargetName) throws ODataJPAModelException;

  /**
   * Find an entity set or singleton based on the external name. Entity sets or singletons marked as with EdmIgnore are
   * ignored
   * @param edmTargetName
   * @return
   * @throws ODataJPAModelException
   */
  Optional<JPATopLevelEntity> getTopLevelEntity(@Nonnull final String edmTargetName) throws ODataJPAModelException;

  List<EdmxReference> getReferences();

  CsdlTerm getTerm(final FullQualifiedName termName);

  @CheckForNull
  JPAStructuredType getComplexType(final EdmComplexType edmComplexType);

  @CheckForNull
  JPAStructuredType getComplexType(Class<?> typeClass);

  @CheckForNull
  JPAEnumerationAttribute getEnumType(final EdmEnumType type);

  @CheckForNull
  JPAEnumerationAttribute getEnumType(final String fqnAsString);

  JPAEdmNameBuilder getNameBuilder();

  /**
   * Returns a map of claims by claim names. It can be used e.g. to convert the values of a JWT token. In case the same
   * claim name is used multiple time just one occurrence is returned, assuming that they have the same type.
   * @return
   * @throws ODataJPAModelException
   */
  Map<String, JPAProtectionInfo> getClaims() throws ODataJPAModelException;

}