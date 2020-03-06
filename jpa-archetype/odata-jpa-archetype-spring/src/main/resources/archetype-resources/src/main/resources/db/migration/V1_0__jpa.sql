SET schema "${schema}";

CREATE TABLE "${entity-table}"(
	"ID" VARCHAR(32) NOT NULL ,
	 PRIMARY KEY ("ID"));
	 
CREATE TABLE "${value-object-table}"(
	"Entity" VARCHAR(32) NOT NULL ,
	"ID" VARCHAR(32) NOT NULL ,
	 PRIMARY KEY ("Entity", "ID"));	 