package com.sap.olingo.jpa.processor.cb.impl;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlPagingFunctions {
  public final PagingFunction LIMIT;
  public final PagingFunction OFFSET;

  public SqlPagingFunctions() {
    LIMIT = new PagingFunction("LIMIT", Integer.MAX_VALUE);
    OFFSET = new PagingFunction("OFFSET", 0);
  }

  public SqlPagingFunctions(Connection conn) {
    boolean issybase = false;
    try {
      String driver = conn.getMetaData().getURL();
      issybase = driver.startsWith("jdbc:sybase");
    } catch ( SQLException e ) {
      e.printStackTrace();        
    }
    if ( issybase ) {  
      LIMIT = new PagingFunction("ROWS LIMIT", Integer.MAX_VALUE);
    } else {
      LIMIT = new PagingFunction("LIMIT", Integer.MAX_VALUE);
    }
    OFFSET = new PagingFunction("OFFSET", 0);
  }

  public class PagingFunction {
    private final String keyWord;
    private final int defaultValue;

    public PagingFunction(final String keyWord, final int defaultValue) {
      this.keyWord = keyWord;
      this.defaultValue = defaultValue;
    }
  
    @Override
    public String toString() {
      return keyWord;
    }
  
    int defaultValue() {
      return defaultValue;
    
    }
  }
}


