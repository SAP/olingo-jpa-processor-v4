package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateModelItemAccess;

abstract class IntermediateModelElement implements IntermediateModelItemAccess {
  protected static JPAEdmMetadataPostProcessor postProcessor = new DefaultEdmPostProcessor();
  protected static final JPANameBuilder intNameBuilder = new JPANameBuilder();

  static void SetPostProcessor(JPAEdmMetadataPostProcessor pP) {
    postProcessor = pP;
  }

  protected final JPAEdmNameBuilder nameBuilder;
  protected final String internalName;
  private boolean ignore = false;

  private String externalName;

  public IntermediateModelElement(JPAEdmNameBuilder nameBuilder, String internalName) {
    super();
    this.nameBuilder = nameBuilder;
    this.internalName = internalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#getExternalName()
   */
  @Override
  public String getExternalName() {
    return externalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#getInternalName()
   */
  @Override
  public String getInternalName() {
    return internalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#ignore()
   */
  @Override
  public boolean ignore() {
    return ignore;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#setExternalName(java.lang.String)
   */
  @Override
  public void setExternalName(String externalName) {
    this.externalName = externalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.odata4.jpa.processor.core.edm.mapper.IntermediatModelItem#setIgnore(boolean)
   */
  @Override
  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }

  protected abstract void lazyBuildEdmItem() throws ODataJPAModelException;

  @SuppressWarnings("unchecked")
  protected <T> List<?> extractEdmModelElements(
      HashMap<String, ?> mappingBuffer) throws ODataJPAModelException {
    List<T> extractionTarget = new ArrayList<T>();
    for (String externalName : mappingBuffer.keySet()) {
      if (!((IntermediateModelElement) mappingBuffer.get(externalName)).ignore)
        extractionTarget.add((T) ((IntermediateModelElement) mappingBuffer.get(externalName)).getEdmItem());
    }
    return returnNullIfEmpty(extractionTarget);
  }

  protected IntermediateModelElement findModelElementByEdmItem(String edmEntityItemName,
      HashMap<String, ?> buffer) throws ODataJPAModelException {
    for (String internalName : buffer.keySet()) {
      IntermediateModelElement modelElement = (IntermediateModelElement) buffer.get(internalName);
      if (edmEntityItemName.equals(modelElement.getExternalName())) {
        return modelElement;
      }
    }
    return null;

  }

  protected <T> List<T> returnNullIfEmpty(List<T> list) {
    return list == null || list.isEmpty() ? null : list;
  }

  abstract CsdlAbstractEdmItem getEdmItem() throws ODataJPAModelException;
}
