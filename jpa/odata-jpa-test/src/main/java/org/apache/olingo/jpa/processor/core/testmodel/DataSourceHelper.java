package org.apache.olingo.jpa.processor.core.testmodel;

import javax.sql.DataSource;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

public class DataSourceHelper {
  private static final String DB_SCHEMA = "OLINGO";
  private static final String H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
  private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
  private static final String HSQLDB_URL = "jdbc:hsqldb:mem:com.sample";
  private static final String HSQLDB_DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";

  public static final int DB_H2 = 1;
  public static final int DB_HSQLDB = 2;

  public static DataSource createDataSource(int database) {
    DriverDataSource ds = null;
    switch (database) {
    case DB_H2:
      ds = new DriverDataSource(H2_DRIVER_CLASS_NAME, H2_URL, null, null, new String[0]);
      break;

    case DB_HSQLDB:
      ds = new DriverDataSource(HSQLDB_DRIVER_CLASS_NAME, HSQLDB_URL, null, null, new String[0]);
      break;

    default:
      return null;
    }

    Flyway flyway = new Flyway();
    flyway.setDataSource(ds);
    flyway.setInitOnMigrate(true);
    flyway.setSchemas(DB_SCHEMA);
    flyway.migrate();
    return ds;
  }
}
