package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398024"
 * >OData Version 4.0 Part 3 - 13 Entity Container</a>
 * @author Oliver Grande
 *
 */
//TODO How to handle multiple schemas
class IntermediateEntityContainer extends IntermediateModelElement {
  final private Map<String, IntermediateSchema> schemaList;
  final private Map<String, IntermediateEntitySet> entitySetListInternalKey;

  private CsdlEntityContainer edmContainer;

  IntermediateEntityContainer(final JPAEdmNameBuilder nameBuilder, final Map<String, IntermediateSchema> schemaList)
      throws ODataJPAModelException {
    super(nameBuilder, nameBuilder.buildContainerName());
    this.schemaList = schemaList;
    this.setExternalName(nameBuilder.buildContainerName());
    this.entitySetListInternalKey = new HashMap<String, IntermediateEntitySet>();
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmContainer == null) {
      edmContainer = new CsdlEntityContainer();
      edmContainer.setName(getExternalName());
      edmContainer.setEntitySets(buildEntitySets());
      edmContainer.setFunctionImports(buildFunctionImports());

      // TODO Singleton
      // TODO ActionImport

    }
  }

  @Override
  CsdlEntityContainer getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmContainer;
  }

  IntermediateEntitySet getEntitySet(final String edmEntitySetName) throws ODataJPAModelException {
    lazyBuildEdmItem();
    return (IntermediateEntitySet) findModelElementByEdmItem(edmEntitySetName,
        entitySetListInternalKey);
  }

  JPAElement getEntitySet(final JPAEntityType entityType) throws ODataJPAModelException {
    lazyBuildEdmItem();
    for (final String internalName : entitySetListInternalKey.keySet()) {
      final IntermediateEntitySet modelElement = entitySetListInternalKey.get(internalName);
      if (modelElement.getEdmItem().getTypeFQN().equals(entityType.getExternalFQN())) {
        return modelElement;
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
    for (final String namespace : schemaList.keySet()) {
      // Build Entity Sets
      final IntermediateSchema schema = schemaList.get(namespace);
      for (final IntermediateEntityType et : schema.getEntityTypes()) {
        if (!et.ignore()) {
          final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et);
          entitySetListInternalKey.put(es.internalName, es);
        }
      }
    }
    return (List<CsdlEntitySet>) extractEdmModelElements(entitySetListInternalKey);
  }

  /**
   * Creates the FunctionImports. Function Imports have to be created for <i>unbound</i> functions. These are functions,
   * which do not depend on an entity set. E.g. .../MyFunction(). <p>
   * Details are described in : <a href=
   * " https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398042"
   * >OData Version 4.0 Part 3 - 13.6 Element edm:FunctionImport</a>
   * @param CsdlFunction edmFu
   */
  private CsdlFunctionImport buildFunctionImport(final CsdlFunction edmFu) {
    final CsdlFunctionImport edmFuImport = new CsdlFunctionImport();
    edmFuImport.setName(edmFu.getName());
    edmFuImport.setFunction(nameBuilder.buildFQN(edmFu.getName()));
    edmFuImport.setIncludeInServiceDocument(true);

    for (final String internalName : entitySetListInternalKey.keySet()) {
      final IntermediateEntitySet entitySet = entitySetListInternalKey.get(internalName);
      if (entitySet.getEntityType().getExternalFQN().equals(edmFu.getReturnType().getTypeFQN())) {
        edmFuImport.setEntitySet(entitySet.getExternalName());
        break;
      }
    }
    return edmFuImport;
  }

  private List<CsdlFunctionImport> buildFunctionImports() throws ODataJPAModelException {
    final List<CsdlFunctionImport> edmFunctionImports = new ArrayList<CsdlFunctionImport>();

    for (final String namespace : schemaList.keySet()) {
      // Build Entity Sets
      final IntermediateSchema schema = schemaList.get(namespace);
      final List<JPAFunction> functions = schema.getFunctions();

      if (functions != null) {
        for (final JPAFunction jpaFu : functions) {
          if (((IntermediateFunction) jpaFu).isBound() == false && ((IntermediateFunction) jpaFu).hasFunctionImport())
            edmFunctionImports.add(buildFunctionImport(((IntermediateFunction) jpaFu).getEdmItem()));
        }
      }
    }
    return edmFunctionImports;
  }

}
