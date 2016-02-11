package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
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
  final private HashMap<String, IntermediateSchema> schemaList;
  final private HashMap<String, IntermediateEntitySet> entitySetListInternalKey;

  private CsdlEntityContainer edmContainer;

  IntermediateEntityContainer(JPAEdmNameBuilder nameBuilder, HashMap<String, IntermediateSchema> schemaList)
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

  IntermediateEntitySet getEntityTypeSet(String edmEntitySetName) throws ODataJPAModelException {
    return (IntermediateEntitySet) findModelElementByEdmItem(edmEntitySetName,
        entitySetListInternalKey);
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
    for (String namespace : schemaList.keySet()) {
      // Build Entity Sets
      IntermediateSchema schema = schemaList.get(namespace);
      for (IntermediateEntityType et : schema.getEntityTypes()) {
        IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et);
        entitySetListInternalKey.put(es.internalName, es);
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
  private CsdlFunctionImport buildFunctionImport(CsdlFunction edmFu) {
    CsdlFunctionImport edmFuImport = new CsdlFunctionImport();
    edmFuImport.setName(edmFu.getName());
    edmFuImport.setFunction(nameBuilder.buildFQN(edmFu.getName()));
    edmFuImport.setIncludeInServiceDocument(true);
    return edmFuImport;
  }

  private List<CsdlFunctionImport> buildFunctionImports() throws ODataJPAModelException {
    List<CsdlFunctionImport> edmFunctionImports = new ArrayList<CsdlFunctionImport>();

    for (String namespace : schemaList.keySet()) {
      // Build Entity Sets
      IntermediateSchema schema = schemaList.get(namespace);
      List<CsdlFunction> functions = schema.getEdmItem().getFunctions();
      if (functions != null) {
        for (CsdlFunction edmFu : functions) {
          edmFunctionImports.add(buildFunctionImport(edmFu));
        }
      }
    }
    return edmFunctionImports;
  }
}
