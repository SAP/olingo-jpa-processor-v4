package org.apache.olingo.jpa.processor.core.util;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.debug.DebugSupport;

public class JPAODataContextAccessDouble implements JPAODataSessionContextAccess {
  private final JPAEdmProvider edmProvider;
  private final DataSource ds;

  public JPAODataContextAccessDouble(JPAEdmProvider edmProvider, DataSource ds) {
    super();
    this.edmProvider = edmProvider;
    this.ds = ds;
  }

  @Override
  public List<EdmxReference> getReferences() {
    fail();
    return null;
  }

  @Override
  public DebugSupport getDebugSupport() {
    fail();
    return null;
  }

  @Override
  public JPAOperationConverter getOperationConverter() {
    fail();
    return null;
  }

  @Override
  public JPAEdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    try {
      return new JPAODataDatabaseProcessorFactory().create(ds);
    } catch (SQLException e) {
      fail();
    }
    return null;
  }

}
