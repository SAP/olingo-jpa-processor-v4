/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

import jakarta.persistence.Tuple;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class FullNameCalculator implements EdmTransientPropertyCalculator<String> {

  @Override
  public String calculateProperty(final Tuple row) {
    final Person dummyPerson = new Person();
    dummyPerson.setFirstName((String) row.get("FirstName"));
    dummyPerson.setLastName((String) row.get("LastName"));
    return dummyPerson.getFullName();
  }

}
