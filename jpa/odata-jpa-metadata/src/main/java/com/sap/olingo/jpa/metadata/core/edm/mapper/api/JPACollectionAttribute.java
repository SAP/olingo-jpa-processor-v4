package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPACollectionAttribute extends JPAAttribute {

  JPAAssociationPath asAssociation() throws ODataJPAModelException;

  /**
   * Returns for simple collections attributes the corresponding attribute of the target entity type. E.g. the following
   * property definition: <br><br>
   * <code>
   * &#64ElementCollection(fetch = FetchType.LAZY)<br>
   * &#64CollectionTable(name = "\"Comment\"", <br>
   * &nbsp&nbsp&nbsp&nbspjoinColumns = &#64@JoinColumn(name = "\"BusinessPartnerID\""))<br>
   * &#64Column(name = "\"Text\"")<br>
   * private List<String> comment = new ArrayList<>();
   * </code><br><br>
   * creates a simple collection attribute. For this collection attribute jpa processor requires that a corresponding
   * entity exists. This entity has to have a property pointing to the same database column, which is returned.
   * 
   * @return In case of simple collections attributes the corresponding attribute of the target entity type otherwise
   * null.
   * @throws ODataJPAModelException
   */
  JPAAttribute getTargetAttribute() throws ODataJPAModelException;
}
