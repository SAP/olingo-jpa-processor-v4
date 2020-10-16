SET schema "${schema}";

CREATE SEQUENCE "TemplateId";

CREATE TABLE "${entity-table}"(
	"ID" BIGINT NOT NULL ,
	"Data" VARCHAR(255),
	 PRIMARY KEY ("ID"));
	 
CREATE TABLE "${value-object-table}"(
	"Entity" BIGINT NOT NULL ,
	"ID" VARCHAR(32) NOT NULL ,
	"Data" VARCHAR(255),
	 PRIMARY KEY ("Entity", "ID"));	 