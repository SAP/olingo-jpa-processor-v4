package org.apache.olingo.jpa.processor.core.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;

public class JPAODataDatabaseProcessorFactory {
  private static final String PRODUCT_NAME_H2 = "H2";
  private static final String PRODUCT_NAME_HSQLDB = "HSQL Database Engine";
  private static final String PRODUCT_NAME_SAP_HANA = "HDB";

  public JPAODataDatabaseProcessor create(final DataSource ds) throws SQLException {
    if (ds != null) {
      final Connection connection = ds.getConnection();
      final DatabaseMetaData dbMetadata = connection.getMetaData();
      connection.close();
      if (dbMetadata.getDatabaseProductName().equals(PRODUCT_NAME_SAP_HANA))
        return new JPADefaultDatabaseProcessor();
      else if (dbMetadata.getDatabaseProductName().equals(PRODUCT_NAME_HSQLDB))
        return new JPA_HSQLDB_DatabaseProcessor();
      else if (dbMetadata.getDatabaseProductName().equals(PRODUCT_NAME_H2))
        return new JPA_HSQLDB_DatabaseProcessor();
      else
        return new JPADefaultDatabaseProcessor();
    } else
      return new JPADefaultDatabaseProcessor();
  }
}
