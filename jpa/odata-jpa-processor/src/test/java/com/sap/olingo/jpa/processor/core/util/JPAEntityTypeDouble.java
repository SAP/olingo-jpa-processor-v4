package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEtagValidator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceInformation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class JPAEntityTypeDouble implements JPAEntityType {
  private final JPAEntityType base;

  public JPAEntityTypeDouble(final JPAEntityType base) {
    super();
    this.base = base;
  }

  @Override
  public JPAAssociationAttribute getAssociation(final String internalName) throws ODataJPAModelException {
    return base.getAssociation(internalName);
  }

  @Override
  public JPAAssociationPath getAssociationPath(final String externalName) throws ODataJPAModelException {
    return base.getAssociationPath(externalName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException {
    return (List<JPAAssociationPath>) failWithNull();
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    fail();
    return Optional.empty();
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName) throws ODataJPAModelException {
    return base.getAttribute(internalName);
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName, final boolean respectIgnore)
      throws ODataJPAModelException {
    return base.getAttribute(internalName, respectIgnore);
  }

  @Override
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException {
    return base.getAttributes();
  }

  @Override
  public List<JPAPath> getCollectionAttributesPath() throws ODataJPAModelException {
    fail();
    return Collections.emptyList();
  }

  @Override
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException {
    fail();
    return Collections.emptyList();
  }

  @Override
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException {
    return base.getDeclaredAttributes();
  }

  @Override
  public Optional<JPAAttribute> getDeclaredAttribute(final String internalName) throws ODataJPAModelException {
    return base.getDeclaredAttribute(internalName);
  }

  @Override
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException {
    fail();
    return Collections.emptyList();
  }

  @Override
  public JPAPath getPath(final String externalName) throws ODataJPAModelException {
    return base.getPath(externalName);
  }

  @Override
  public JPAPath getPath(final String externalName, final boolean respectIgnore) throws ODataJPAModelException {
    return (JPAPath) failWithNull();
  }

  @Override
  public List<JPAPath> getPathList() throws ODataJPAModelException {
    return base.getPathList();
  }

  @Override
  public Class<?> getTypeClass() {
    return base.getTypeClass();
  }

  @Override
  public boolean isAbstract() {
    return failWithFalse();
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return base.getExternalFQN();
  }

  @Override
  public String getExternalName() {
    return base.getExternalName();
  }

  @Override
  public String getInternalName() {
    return base.getInternalName();
  }

  @Override
  public String getContentType() throws ODataJPAModelException {
    return (String) failWithNull();
  }

  @Override
  public JPAPath getContentTypeAttributePath() throws ODataJPAModelException {
    return (JPAPath) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<JPAAttribute> getKey() throws ODataJPAModelException {
    return (List<JPAAttribute>) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    return (List<JPAPath>) failWithNull();
  }

  @Override
  public Class<?> getKeyType() {
    return base.getKeyType();
  }

  @Override
  public List<JPAPath> getSearchablePath() throws ODataJPAModelException {
    return base.getSearchablePath();
  }

  @Override
  public JPAPath getStreamAttributePath() throws ODataJPAModelException {
    return base.getStreamAttributePath();
  }

  @Override
  public String getTableName() {
    return base.getTableName();
  }

  @Override
  public boolean hasEtag() throws ODataJPAModelException {
    return base.hasEtag();
  }

  @Override
  public boolean hasStream() throws ODataJPAModelException {
    return base.hasStream();
  }

  @Override
  public List<JPAPath> searchChildPath(final JPAPath selectItemPath) throws ODataJPAModelException {
    return base.searchChildPath(selectItemPath);
  }

  @Override
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException {
    return base.getCollectionAttribute(externalName);
  }

  @Override
  public List<JPAProtectionInfo> getProtections() throws ODataJPAModelException {
    return base.getProtections();
  }

  @Override
  public JPAPath getEtagPath() throws ODataJPAModelException {
    return (JPAPath) failWithNull();
  }

  @Override
  public boolean hasCompoundKey() {
    return failWithFalse();
  }

  @Override
  public boolean hasEmbeddedKey() {
    return failWithFalse();
  }

  @Override
  public <X extends EdmQueryExtensionProvider> Optional<JPAQueryExtension<X>> getQueryExtension()
      throws ODataJPAModelException {
    return Optional.empty();
  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    return this.base.getAnnotation(alias, term);
  }

  @Override
  public JPAStructuredType getBaseType() throws ODataJPAModelException {
    return base.getBaseType();
  }

  @Override
  public JPAInheritanceInformation getInheritanceInformation() throws ODataJPAModelException {
    fail();
    return new JPAInheritanceInformation() {

      @Override
      public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
        return List.of();
      }
    };
  }

  @Override
  public Object getAnnotationValue(final String alias, final String term, final String property)
      throws ODataJPAModelException {
    return this.base.getAnnotationValue(alias, term, property);
  }

  @Override
  public JPAEtagValidator getEtagValidator() {
    fail();
    return JPAEtagValidator.WEAK;
  }

  private Object failWithNull() {
    fail();
    return null;
  }

  private boolean failWithFalse() {
    fail();
    return false;
  }

  @Override
  public List<String> getUserGroups() throws ODataJPAModelException {
    fail();
    return List.of();
  }
}
