/**
 * 
 */
package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class HeaderParamTransientPropertyConverter implements EdmTransientPropertyCalculator<String> {
  private final Map<String, List<String>> header;

  public HeaderParamTransientPropertyConverter(final Map<String, List<String>> header) {
    super();
    this.header = header;
  }

  public Map<String, List<String>> getHeader() {
    return header;
  }
}