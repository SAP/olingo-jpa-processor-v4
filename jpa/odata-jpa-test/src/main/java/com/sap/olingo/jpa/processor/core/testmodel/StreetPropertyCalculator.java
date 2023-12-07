/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class StreetPropertyCalculator implements EdmTransientPropertyCalculator<String> {

  @Override
  public String calculateProperty(final Tuple row) {

    return new StringBuffer()
        .append(row.get("Address/StreetName"))
        .append(" ")
        .append(row.get("Address/HouseNumber"))
        .toString();
  }

}
