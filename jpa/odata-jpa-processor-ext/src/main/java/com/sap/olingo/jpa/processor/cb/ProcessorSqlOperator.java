package com.sap.olingo.jpa.processor.cb;

import java.util.List;

public record ProcessorSqlOperator(List<ProcessorSqlParameter> parameters)
    implements ProcessorSqlPattern {

}
