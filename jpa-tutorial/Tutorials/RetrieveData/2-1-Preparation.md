# 2.1 Preparation
Up to now we only generated the metadata document, which may got a little bit boring. Now we want to retrieve data to see what OData, Olingo and the JPA Processor really can do for use.  
If we want to retrieve data we first have to create the tables on the database and insert some rows. As a preparation we declared right in the beginning a dependency to Flyway.
We use it to process a SQL document. This document must be stored in folder _/src/main/resources/db/migration_ and shall have the name _V1_0__olingo.sql_. Best you just copy it from here: 
[SQL Document](migration/V1_0__olingo.sql).  

The class we have to use for retrieving data, _JPAODataGetHandler_, requires a _javax.sql.DataSource_ and not an _EntityManager_. To make our life a little bit easier, we
create a factory to create Data Sources:
```Java
package tutorial.service;

import java.io.IOException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataSourceHelper {
	private static final String DB_SCHEMA = "OLINGO";

	private static final String HSQLDB_URL = "jdbc:hsqldb:mem:com.sample";
	private static final String HSQLDB_DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";

	private static final String DERBY_URL = "jdbc:derby:test;create=true;traceFile=derby_trace.log;trace_level=0xFFFFFFFF";
	private static final String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

	private static final String REMOTE_URL = "jdbc:$DBNAME$:$Host$:$Port$";

	public static final int DB_HSQLDB = 2;
	public static final int DB_REMOTE = 3;
	public static final int DB_DERBY = 4;

	public static DataSource createDataSource(int database) {
		DriverDataSource ds = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		switch (database) {
		case DB_HSQLDB:
			ds = new DriverDataSource(classLoader, HSQLDB_DRIVER_CLASS_NAME, HSQLDB_URL, null, null, new String[0]);
			break;
		case DB_DERBY:
			ds = new DriverDataSource(classLoader, DERBY_DRIVER_CLASS_NAME, DERBY_URL, null, null, new String[0]);
			break;

		case DB_REMOTE:
			String env = System.getenv().get("REMOTE_DB_LOGON");
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode dbInfo;
			try {
				dbInfo = (ObjectNode) mapper.readTree(env);
			} catch (JsonProcessingException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
			String url = REMOTE_URL;
			url = url.replace("$Host$", dbInfo.get("hostname").asText());
			url = url.replace("$Port$", dbInfo.get("port").asText());
			url = url.replace("$DBNAME$", dbInfo.get("dbname").asText());
			String driver = dbInfo.get("driver").asText();
			ds = new DriverDataSource(classLoader, driver, url, dbInfo.get("username").asText(),
					dbInfo.get("password").asText(), new String[0]);
			return ds;
		default:
			return null;
		}

		Flyway flyway = new Flyway();
		flyway.setDataSource(ds);

		flyway.setSchemas(DB_SCHEMA);
		flyway.migrate();
		return ds;
	}
}
```  
With this, we can rebuild our service implementation and start to retrieve data: [Tutorial 2.1: Retrieving Data](2-1-RetrievingData.md)