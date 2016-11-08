package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.IOException;

import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

public class DataSourceHelper {
  private static final String DB_SCHEMA = "OLINGO";

  private static final String H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
  private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";

  private static final String HSQLDB_URL = "jdbc:hsqldb:mem:com.sample";
  private static final String HSQLDB_DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";

  private static final String DERBY_URL =
      "jdbc:derby:test;create=true;traceFile=derby_trace.log;trace_level=0xFFFFFFFF";
  private static final String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

  private static final String REMOTE_URL = "jdbc:$DBNAME$:$Host$:$Port$";

  public static final int DB_H2 = 1;
  public static final int DB_HSQLDB = 2;
  public static final int DB_REMOTE = 3;
  public static final int DB_DERBY = 4;

  public static DataSource createDataSource(int database) {
    DriverDataSource ds = null;
    switch (database) {
    case DB_H2:
      ds = new DriverDataSource(H2_DRIVER_CLASS_NAME, H2_URL, null, null, new String[0]);
      break;

    case DB_HSQLDB:
      ds = new DriverDataSource(HSQLDB_DRIVER_CLASS_NAME, HSQLDB_URL, null, null, new String[0]);
      break;
    case DB_DERBY:
      ds = new DriverDataSource(DERBY_DRIVER_CLASS_NAME, DERBY_URL, null, null, new String[0]);
      break;

    case DB_REMOTE:
      String env = System.getenv().get("REMOTE_DB_LOGON");
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode remoteInfo;
      try {
        remoteInfo = (ObjectNode) mapper.readTree(env);
      } catch (JsonProcessingException e) {
        return null;
      } catch (IOException e) {
        return null;
      }
      String url = REMOTE_URL;
      url = url.replace("$Host$", remoteInfo.get("hostname").asText());
      url = url.replace("$Port$", remoteInfo.get("port").asText());
      url = url.replace("$DBNAME$", remoteInfo.get("dbname").asText());
      String driver = remoteInfo.get("driver").asText();
      ds = new DriverDataSource(driver, url, remoteInfo.get("username").asText(), remoteInfo.get(
          "password").asText(), new String[0]);
      return ds;
    default:
      return null;
    }

    Flyway flyway = new Flyway();
    // flyway.
    flyway.setDataSource(ds);
    flyway.setInitOnMigrate(true);
    flyway.setSchemas(DB_SCHEMA);
    flyway.migrate();
    return ds;
  }
}
