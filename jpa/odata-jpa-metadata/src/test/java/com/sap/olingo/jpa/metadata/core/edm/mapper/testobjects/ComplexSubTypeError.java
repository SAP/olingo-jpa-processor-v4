package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.SRID;

public class ComplexSubTypeError extends Point {

  public ComplexSubTypeError(final Dimension dimension, final SRID srid) {
    super(dimension, srid);
  }
}
