/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class GroupNameCalculator implements EdmTransientPropertyCalculator<String> {

  @Override
  public String calculateProperty(final Tuple row) {
    final String id = ((String) row.get("ID"));
    final String name = ((String) row.get("Name"));
    return id + " " + name;
  }

}
