package com.sap.olingo.jpa.processor.cb;

import java.util.List;

public record ProcessorSqlFunction(String function, List<ProcessorSqlParameter> parameters)
    implements ProcessorSqlPattern {

}
