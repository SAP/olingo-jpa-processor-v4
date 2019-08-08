# 2.3 Using Functions

In tutorial [Functions](../Metadata/1-8-Functions.md) the metadata for overall three user defined functions have been created. Now we want see what we need to do to use them. 

The required SQL statements to create them on the database are part of the [migration document](../RetrieveData/migration/V1_0__olingo.sql).

## Scalar Functions used as filter
We have created two functions that we want to use for filtering. We can employ them without any further implementations like this:

`.../AdministrativeDivisions?$filter=Tutorial.PopulationDensity(Area=Tutorial.ConvertToQkm(Area=$it/Area),Population=$it/Population) gt 1000`

## Function Imports and Bound Functions

Making queries on functions is more difficult. The reason for this is that JPA, at least till version 2.1, does not supported this and that different databases have a different syntax for those queries. Two pattern have been found so far:

* `SELECT * FROM TABLE (<FUNCTIONNAME>(<PARAMETER LIST>))`: E.g. HSQLDB, Derby
* `SELECT * FROM <FUNCTIONNAME>(<PARAMETER LIST>)`: E.g. SAP HANA,  PostgreSQL

This requires a database specific implementation. The JPA Processor provides the interface 
[JPAODataDatabaseProcessor](../../../jpa/odata-jpa-processor/src/main/java/com/sap/olingo/jpa/processor/core/api/JPAODataDatabaseProcessor.java) for this. As of now the interface is in __beta__ state. For this tutorial we want to copy class [JPA_HSQLDB_DatabaseProcessor](../../../jpa/odata-jpa-processor/src/main/java/com/sap/olingo/jpa/processor/core/database/JPA_HSQLDB_DatabaseProcessor.java), beta as well, to package `tutorial.service` with the new name `HSQLDatabaseProcessor`. After that we need to register the database processor at our service context. This is done, within the `Listener`:

```Java
		...
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    try {
      final JPAODataCRUDContextAccess serviceContext = JPAODataServiceContext.with()
          .setPUnit(PUNIT_NAME)
          .setDataSource(ds)
          .setTypePackage("tutorial.operations", "tutorial.model")
          .setDatabaseProcessor(new HSQLDatabaseProcessor())
          .build();
      sce.getServletContext().setAttribute("ServiceContext", serviceContext);
    } catch (ODataException e) {
      // Log error
    }
  }
		...
```
Having done that we can call the Siblings function:

`../Siblings(DivisionCode='BE251',CodeID='NUTS3',CodePublisher='Eurostat')`

In case we would have marked the function as bound, the call would be:

`../AdministrativeDivisions(DivisionCode='BE251',CodeID='NUTS3',CodePublisher='Eurostat')/Tutorial.Siblings()`

Currently [Server Driven Paging](../SpecialTopics/4-3-ServerDrivenPaging.md), is not supported for Functions.

If you like, you can go ahead and learn how you can  [Change Data](../ChangeData/3-0-Overview.md) .