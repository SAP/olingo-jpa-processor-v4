# Criteria Builder Implementation

__This project is under construction! Feel free to try it__

JPQL shall be converted into SQL that can be interpreted by a wide range of database. As such it expressivity of JPQL is limited. This limitation can lead to performance problems. E.g. JPQL does not support LIMIT and OFFSET for sub-queries and does not support window functions. So queries like:

```SQL
SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "Area", "Population"
	FROM "OLINGO"."AdministrativeDivision"
	WHERE ("CodePublisher", "ParentCodeID", "ParentDivisionCode") 
		IN (SELECT "CodePublisher", "CodeID" as "ParentCodeID", "DivisionCode" as "ParentDivisionCode"
				FROM "OLINGO"."AdministrativeDivision"
				WHERE "CodePublisher" = 'Eurostat'
				AND "CodeID" = 'NUTS1'
				ORDER BY  "DivisionCode"
				LIMIT 2);
```

or

```SQL
SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "Area", "Population"
	FROM ( SELECT "CodePublisher", "CodeID", "DivisionCode", "CountryISOCode", "Area", "Population",
				   ROW_NUMBER () 
					OVER ( PARTITION BY "CodePublisher", "CodeID"
							 ORDER BY "CodePublisher", "CodeID" ) AS row_no
				  FROM "OLINGO"."AdministrativeDivision" ) AS i
	WHERE i.row_no BETWEEN 0 AND 2;
```

are not supported. This type of queries are quite handy for client side or service driven paging.

This project contains an implementation of a set of interfaces from `jakarta.persistence.criteria.*` to enable the creation queries like mentioned above. This implementation is used if it is part of the build path or in other words if it can be found by the class loader.