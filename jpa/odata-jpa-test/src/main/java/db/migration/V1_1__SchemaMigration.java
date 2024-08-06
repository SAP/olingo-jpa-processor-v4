package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.database.DatabaseType;

/**
 *
 *
 *
 * Created 2024-06-09
 * @author Oliver Grande
 * @since 2.1.2
 *
 */
public class V1_1__SchemaMigration extends BaseJavaMigration { // NOSONAR

  @Override
  public void migrate(final Context context) throws Exception {
    final var configuration = context.getConfiguration();
    final var connection = context.getConnection();
    final DatabaseType dbType = configuration.getDatabaseType();
    final List<Optional<PreparedStatement>> preparedStatements = switch (dbType.getName()) {
      case "H2" -> createFunctionH2();
      case "SAP HANA" -> createFunctionHANA(connection);
      case "HSQLDB" -> createFunctionHSQLDB(connection);
      case "PostgreSQL" -> createFunctionPostgres(connection);
      case "Derby" -> createFunctionDerby();
      default -> raiseUnsupportedDbException(dbType);

    };

    for (final var preparedStatement : preparedStatements) {
      if (preparedStatement.isPresent()) {
        preparedStatement.get().execute();
        preparedStatement.get().close();
      }
    }
  }

  private List<Optional<PreparedStatement>> raiseUnsupportedDbException(final DatabaseType dbType) {
    throw new IllegalArgumentException("No migration for database of type: " + dbType.getName());
  }

  private List<Optional<PreparedStatement>> createFunctionPostgres(final Connection connection) throws SQLException {
    final String sql =
        """
            CREATE OR REPLACE FUNCTION "OLINGO"."Siblings"("CodePublisher" character varying, "CodeID" character varying, "DivisionCode" character varying)
              RETURNS SETOF "OLINGO"."AdministrativeDivision"
              LANGUAGE sql
              AS $function$
                SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "ParentCodeID", "ParentDivisionCode", "AlternativeCode", "Area", "Population"
                  FROM "OLINGO"."AdministrativeDivision" as kids
                  WHERE ("CodePublisher", "ParentCodeID", "ParentDivisionCode") IN
                    (SELECT "CodePublisher", "ParentCodeID", "ParentDivisionCode"
                        FROM  "OLINGO"."AdministrativeDivision"
                        WHERE "CodePublisher" = $1
                        AND "CodeID"  =  $2
                        AND "DivisionCode" = $3 )
                  AND kids."DivisionCode" <> $3;
            $function$
            ;
              """;
    return Collections.singletonList(Optional.of(connection.prepareStatement(sql)));
  }

  private List<Optional<PreparedStatement>> createFunctionHSQLDB(final Connection connection) throws SQLException {

    final String sqlSiblings =
        """
            CREATE FUNCTION "OLINGO"."Siblings" ("Publisher" VARCHAR(10), "ID" VARCHAR(10), "Division" VARCHAR(10))
              RETURNS TABLE(
                "CodePublisher" VARCHAR(10),
                "CodeID" VARCHAR(10),
                "DivisionCode" VARCHAR(10),
                "CountryISOCode" VARCHAR(4),
                "ParentCodeID" VARCHAR(10),
                "ParentDivisionCode" VARCHAR(10),
                "AlternativeCode" VARCHAR(10),
                "Area" int,
                "Population" BIGINT)
              READS SQL DATA
              RETURN TABLE( SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "ParentCodeID", "ParentDivisionCode", "AlternativeCode", "Area", "Population"
                        FROM "AdministrativeDivision" as a
                        WHERE
                          EXISTS (SELECT "CodePublisher"
                                  FROM "AdministrativeDivision" as b
                                  WHERE b."CodeID" = "ID"
                                  AND   b."DivisionCode" = "Division"
                                  AND   b."CodePublisher" = a."CodePublisher"
                                  AND   b."ParentCodeID" = a."ParentCodeID"
                                  AND   b."ParentDivisionCode" = a."ParentDivisionCode")
                          AND NOT( a."CodePublisher" = "Publisher"
                        AND  a."CodeID" = "ID"
                        AND  a."DivisionCode" = "Division" )
                    );
              """;
    final String sqlPopulationDensity = """
        CREATE FUNCTION  "OLINGO"."PopulationDensity" (UnitArea BIGINT, Population BIGINT )
          RETURNS DOUBLE
          BEGIN ATOMIC
            DECLARE areaDouble DOUBLE;
            DECLARE populationDouble DOUBLE;
            SET areaDouble = UnitArea;
            SET populationDouble = Population;
            IF UnitArea <= 0 THEN
              RETURN 0;
            ELSE
              RETURN populationDouble / areaDouble;
            END IF;
          END
         """;
    final String sqlConvertToQkm = """
        CREATE FUNCTION  "OLINGO"."ConvertToQkm" (UnitArea BIGINT )
          RETURNS INT
          IF UnitArea <= 0 THEN RETURN 0;
          ELSE RETURN UnitArea / 1000 / 1000;
          END IF
         """;

    return Arrays.asList(
        Optional.of(connection.prepareStatement(sqlSiblings)),
        Optional.of(connection.prepareStatement(sqlConvertToQkm)),
        Optional.of(connection.prepareStatement(sqlPopulationDensity)));
  }

  private List<Optional<PreparedStatement>> createFunctionHANA(final Connection connection) throws SQLException {
    final String sql =
        """
            CREATE FUNCTION "OLINGO"."Siblings" ("CodePublisher" VARCHAR(10), "CodeID" VARCHAR(10), "DivisionCode" VARCHAR(10))
              RETURNS TABLE (
                "CodePublisher" VARCHAR(10), "CodeID" VARCHAR(10), "DivisionCode" VARCHAR(10),
                "CountryISOCode" VARCHAR(4), "ParentCodeID" VARCHAR(10), "ParentDivisionCode" VARCHAR(10),
                "AlternativeCode" VARCHAR(10), "Area" BIGINT,  "Population" BIGINT)
              LANGUAGE SQLSCRIPT AS
              BEGIN
                RETURN SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "ParentCodeID", "ParentDivisionCode", "AlternativeCode", "Area", "Population"
                  FROM "OLINGO"."AdministrativeDivision" as kids
                  WHERE ("CodePublisher", "ParentCodeID", "ParentDivisionCode") IN
                    (SELECT "CodePublisher", "ParentCodeID", "ParentDivisionCode"
                            FROM  "OLINGO"."AdministrativeDivision"
                        WHERE "CodePublisher" = :"CodePublisher"
                        AND "CodeID"  =  :"CodeID"
                        AND "DivisionCode" = :"DivisionCode" )
                  AND kids."DivisionCode" <> :"DivisionCode";
              END;
              """;
    return Arrays.asList(Optional.of(connection.prepareStatement(sql)));
  }

  private List<Optional<PreparedStatement>> createFunctionDerby() {

    return Collections.emptyList();
  }

  private List<Optional<PreparedStatement>> createFunctionH2() {
    return Collections.emptyList();
  }

}
