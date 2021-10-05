package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityContainerAccess;

/**
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398024"
 * >OData Version 4.0 Part 3 - 13 Entity Container</a>
 * @author Oliver Grande
 *
 */
//TODO How to handle multiple schemas
final class IntermediateEntityContainer extends IntermediateModelElement implements IntermediateEntityContainerAccess {
  private final Map<String, IntermediateSchema> schemaList;
  private final Map<String, IntermediateEntitySet> entitySetListInternalKey;
  private final Map<String, IntermediateSingleton> singletonListInternalKey;

  private CsdlEntityContainer edmContainer;

  IntermediateEntityContainer(final JPAEdmNameBuilder nameBuilder, final Map<String, IntermediateSchema> schemaList) {
    super(nameBuilder, nameBuilder.buildContainerName());
    this.schemaList = schemaList;
    this.setExternalName(nameBuilder.buildContainerName());
    this.entitySetListInternalKey = new HashMap<>();
    this.singletonListInternalKey = new HashMap<>();
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmContainer == null) {
      postProcessor.processEntityContainer(this);
      edmContainer = new CsdlEntityContainer();
      edmContainer.setName(getExternalName());
      edmContainer.setEntitySets(buildEntitySets());
      edmContainer.setFunctionImports(buildFunctionImports());
      edmContainer.setActionImports(buildActionImports());
      edmContainer.setAnnotations(edmAnnotations);
      edmContainer.setSingletons(buildSingletons());
    }
  }

  @Override
  CsdlEntityContainer getEdmItem() throws ODataJPAModelException {
    if (edmContainer == null) {
      lazyBuildEdmItem();
    }
    return edmContainer;
  }

  IntermediateEntitySet getEntitySet(final String edmEntitySetName) throws ODataJPAModelException {
    if (edmContainer == null) {
      lazyBuildEdmItem();
    }
    return (IntermediateEntitySet) findModelElementByEdmItem(edmEntitySetName,
        entitySetListInternalKey);
  }

  IntermediateSingleton getSingleton(final String edmSingletonName) throws ODataJPAModelException {
    if (edmContainer == null) {
      lazyBuildEdmItem();
    }
    return (IntermediateSingleton) findModelElementByEdmItem(edmSingletonName, singletonListInternalKey);
  }

  /**
   * Internal Entity Type
   * @param entityType
   * @return
   * @throws ODataJPAModelException
   */
  JPAEntitySet getEntitySet(final JPAEntityType entityType) throws ODataJPAModelException {
    if (edmContainer == null) {
      lazyBuildEdmItem();
    }
    for (final Entry<String, IntermediateEntitySet> entitySet : entitySetListInternalKey.entrySet()) {
      if (entitySet.getValue().getEntityType().getExternalFQN().equals(entityType.getExternalFQN())) {
        return entitySet.getValue();
      }
    }
    return null;
  }

  /**
   * Entity Sets are described in <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398024"
   * >OData Version 4.0 Part 3 - 13.2 Element edm:EntitySet</a>
   * @param Entity Type
   * @return Entity Set
   */
  @SuppressWarnings("unchecked")
  private List<CsdlEntitySet> buildEntitySets() throws ODataJPAModelException {
    for (final Entry<String, IntermediateSchema> schema : schemaList.entrySet()) {
      for (final IntermediateEntityType<?> et : schema.getValue().getEntityTypes()) {
        if ((!et.ignore() || et.asTopLevelOnly()) && et.asEntitySet()) {
          final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et);
          entitySetListInternalKey.put(es.internalName, es);
        }
      }
    }
    return (List<CsdlEntitySet>) extractEdmModelElements(entitySetListInternalKey);
  }

  /**
   *
   * @return List of Singletons
   * @throws ODataJPAModelException
   */
  @SuppressWarnings("unchecked")
  private List<CsdlSingleton> buildSingletons() throws ODataJPAModelException {
    for (final Entry<String, IntermediateSchema> schema : schemaList.entrySet()) {
      for (final IntermediateEntityType<?> et : schema.getValue().getEntityTypes()) {
        if ((!et.ignore() || et.asTopLevelOnly()) && et.asSingleton()) {
          final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
          singletonListInternalKey.put(singleton.internalName, singleton);
        }
      }
    }
    return (List<CsdlSingleton>) extractEdmModelElements(singletonListInternalKey);
  }

  /**
   * Creates the FunctionImports. Function Imports have to be created for <i>unbound</i> functions. These are functions,
   * which do not depend on an entity set. E.g. .../MyFunction(). <p>
   * Details are described in : <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398042"
   * >OData Version 4.0 Part 3 - 13.6 Element edm:FunctionImport</a>
   * @param CsdlFunction edmFu
   */
  private CsdlFunctionImport buildFunctionImport(final CsdlFunction edmFu) {
    final CsdlFunctionImport edmFuImport = new CsdlFunctionImport();
    edmFuImport.setName(edmFu.getName());
    edmFuImport.setFunction(buildFQN(edmFu.getName()));
    edmFuImport.setIncludeInServiceDocument(true);
    // edmFuImport.setEntitySet(entitySet)

    return edmFuImport;
  }

  private List<CsdlFunctionImport> buildFunctionImports() throws ODataJPAModelException {
    final List<CsdlFunctionImport> edmFunctionImports = new ArrayList<>();

    for (final Entry<String, IntermediateSchema> namespace : schemaList.entrySet()) {
      // Build Entity Sets
      final IntermediateSchema schema = namespace.getValue();
      final List<JPAFunction> functions = schema.getFunctions();

      for (final JPAFunction jpaFu : functions) {
        if (!((IntermediateFunction) jpaFu).isBound() && ((IntermediateFunction) jpaFu).hasImport())
          edmFunctionImports.add(buildFunctionImport(((IntermediateFunction) jpaFu).getEdmItem()));
      }
    }
    return edmFunctionImports;
  }

  private List<CsdlActionImport> buildActionImports() throws ODataJPAModelException {
    final List<CsdlActionImport> edmActionImports = new ArrayList<>();

    for (final Entry<String, IntermediateSchema> namespace : schemaList.entrySet()) {
      // Build Entity Sets
      final IntermediateSchema schema = namespace.getValue();
      final List<JPAAction> actions = schema.getActions();

      for (final JPAAction jpaAc : actions) {
        if (((IntermediateJavaAction) jpaAc).hasImport())
          edmActionImports.add(buildActionImport(((IntermediateJavaAction) jpaAc).getEdmItem()));
      }
    }
    return edmActionImports;
  }

  /**
   * Creates the ActionImports. Function Imports have to be created for <i>unbound</i> actions. These are actions,
   * which do not depend on an entity set. E.g. .../MyAction(). <p>
   * Details are described in : <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-
   * complete.html#_Toc406398038">13.5 Element edm:ActionImport</a>
   * @param edmAc
   * @return
   */
  private CsdlActionImport buildActionImport(final CsdlAction edmAc) {
    final CsdlActionImport edmAcImport = new CsdlActionImport();
    edmAcImport.setName(edmAc.getName());
    edmAcImport.setAction(buildFQN(edmAc.getName()));
    // edmAcImport.setEntitySet(entitySet)
    return edmAcImport;
  }

}
