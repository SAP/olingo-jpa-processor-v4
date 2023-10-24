package com.sap.olingo.jpa.processor.cb.impl;

import jakarta.persistence.criteria.Selection;

import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

interface SqlSelection<X> extends ProcessorSelection<X>, SqlConvertible {
  Selection<X> getSelection();
}
