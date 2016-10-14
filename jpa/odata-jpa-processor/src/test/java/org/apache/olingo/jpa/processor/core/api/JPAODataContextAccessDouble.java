package org.apache.olingo.jpa.processor.core.api;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPACUDRequestHandler;
import org.apache.olingo.server.api.debug.DebugSupport;

public class JPAODataContextAccessDouble implements JPAODataSessionContextAccess {
  private final JPAEdmProvider edmProvider;
  private final DataSource ds;
  private final JPAODataDatabaseOperations context;

  public JPAODataContextAccessDouble(final JPAEdmProvider edmProvider, final DataSource ds) {
    super();
    this.edmProvider = edmProvider;
    this.ds = ds;
    this.context = new JPADefaultDatabaseProcessor();
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
  public JPAODataDatabaseOperations getOperationConverter() {
    return context;
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

  @Override
  public JPAServiceDebugger getDebugger() {
    return new JPAEmptyDebugger();
  }

  @Override
  public JPACUDRequestHandler getCUDRequestHandler() {
    fail();
    return null;
  }

}
