package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.IOException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataSourceHelper {
  private static final String DB_SCHEMA = "OLINGO";

  private static final String H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL";
  private static final String HSQLDB_URL = "jdbc:hsqldb:mem:com.sample";
  private static final String DERBY_URL =
      "jdbc:derby:test;create=true;traceFile=derby_trace.log;trace_level=0xFFFFFFFF";
  private static final String REMOTE_URL = "jdbc:$DBNAME$:$Host$:$Port$";

  public static final int DB_H2 = 1;
  public static final int DB_HSQLDB = 2;
  public static final int DB_REMOTE = 3;
  public static final int DB_DERBY = 4;

  private DataSourceHelper() {
    throw new IllegalStateException("JPAEntityManagerFactory class");
  }

  public static DataSource createDataSource(final int database) {
    final DriverDataSource ds = null;
    final FluentConfiguration config = Flyway.configure();
    switch (database) {
      case DB_H2:
        config.dataSource(H2_URL, "default", null);
        break;
      case DB_HSQLDB:
        config.dataSource(HSQLDB_URL, null, null);
        break;
      case DB_DERBY:
        config.dataSource(DERBY_URL, null, null);
        break;
      case DB_REMOTE:
        final String env = System.getenv().get("REMOTE_DB_LOGON");
        final ObjectMapper mapper = new ObjectMapper();
        ObjectNode remoteInfo;
        try {
          remoteInfo = (ObjectNode) mapper.readTree(env);
        } catch (final IOException e) {
          return null;
        }
        String url = REMOTE_URL;
        url = url.replace("$Host$", remoteInfo.get("hostname").asText());
        url = url.replace("$Port$", remoteInfo.get("port").asText());
        url = url.replace("$DBNAME$", remoteInfo.get("dbname").asText());
        config.dataSource(url, remoteInfo.get("username").asText(), remoteInfo.get(
            "password").asText());
        return ds;
      default:
        return null;
    }

    config.schemas(DB_SCHEMA);
    final Flyway flyway = new Flyway(config);
//    flyway.setDataSource(ds);
//    flyway.setInitOnMigrate(true);
//    flyway.setSchemas(DB_SCHEMA);
    flyway.migrate();
    return config.getDataSource();
  }
}
