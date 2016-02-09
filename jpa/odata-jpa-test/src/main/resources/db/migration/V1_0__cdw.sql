
SET schema "OLINGO";
    
CREATE TABLE "org.apache.olingo.jpa::BusinessPartner" (

	"ID" NVARCHAR(32) NOT NULL ,
	"ETag" BIGINT,
	"Type" TINYINT,
	"CustomString1" NVARCHAR(250),
	"CustomString2" NVARCHAR(250),
	"CustomNum1" DECIMAL(34,0),
	"CustomNum2"  DECIMAL(34,0),
	"NameLine1" NVARCHAR(250),
	"NameLine2" NVARCHAR(250),
	"BirthDay" DATE,
	"Address.StreetName" NVARCHAR(200),
    "Address.StreetNumber" NVARCHAR(60),
    "Address.PostOfficeBox" NVARCHAR(60),
    "Address.City" NVARCHAR(100),
    "Address.PostalCode" NVARCHAR(60),
    "Address.RegionCodePublisher" NVARCHAR(10) NOT NULL,
	"Address.RegionCodeID" NVARCHAR(10) NOT NULL,
    "Address.Region" NVARCHAR(100),
    "Address.Country" NVARCHAR(100),
    "Telecom.Phone" NVARCHAR(100),
    "Telecom.Mobile" NVARCHAR(100),
    "Telecom.Fax" NVARCHAR(100),
    "Telecom.Email" NVARCHAR(100),
	"CreatedBy" NVARCHAR(32) NOT NULL ,
	"CreatedAt" TIMESTAMP,   
	"UpdatedBy" NVARCHAR(32) NOT NULL ,
	"UpdatedAt" TIMESTAMP,
    "Country" NVARCHAR(4),
	 PRIMARY KEY ("ID"));
     
insert into "org.apache.olingo.jpa::BusinessPartner" values ('1', 0, 2, '','',null,null,'First Org.','',null,'Test Road', '23','', 'Test City','94321','ISO', '3166-2','US-CA', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('2', 0, 2, '','',null,null,'Second Org.','',null,'Test Road', '45','', 'Test City','76321','ISO', '3166-2','US-TX', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('3', 0, 2, '','',null,null,'Third Org.','',null,'Test Road', '223','', 'Test City','94322','ISO', '3166-2','US-CA', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('4', 0, 2, '','',null,null,'Fourth Org.','',null,'Test Road', '56','', 'Test City','84321','ISO', '3166-2','US-UT', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('5', 0, 2, '','',null,null,'Fifth Org.','',null,'Test Road', '35','', 'Test City','59321','ISO', '3166-2','US-MT', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('6', 0, 2, '','',null,null,'Sixth Org.','',null,'Test Road', '7856','', 'Test City','94324','ISO', '3166-2','US-CA', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('7', 0, 2, '','',null,null,'Seventh Org.','',null,'Test Road', '4','', 'Test City','29321','ISO', '3166-2','US-SC', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('8', 0, 2, '','',null,null,'Eighth Org.','',null,'Test Road', '453','', 'Test City','29221','ISO', '3166-2','US-SC', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('9', 0, 2, '','',null,null,'Ninth Org.','',null,'Test Road', '93','', 'Test City','55021','ISO', '3166-2','US-MN', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'USA');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('10', 0, 2, '','',null,null,'Tenth Org.','',null,'Test Road', '12','', 'Test City','03921','ISO', '3166-2','US-ME', 'USA', '', '','','', '99','2016-01-20 09:21:23', '', null, 'DEU');
insert into "org.apache.olingo.jpa::BusinessPartner" values ('99', 0, 1, '','',null,null,'Max','Mustermann',null,'Test Starße', '12','', 'Teststadt','10115','ISO', '3166-2','DE-BE', 'DEU', '', '','','', '99','2016-01-20 09:21:23', '', null, 'DEU'); 

CREATE TABLE "org.apache.olingo.jpa::BusinessPartnerRole" ( 
	"BusinessPartnerID" NVARCHAR(32) NOT NULL ,
	"BusinessPartnerRoleCategoryCode" NVARCHAR(10) NOT NULL, 
     PRIMARY KEY ("BusinessPartnerID","BusinessPartnerRoleCategoryCode"));

insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('1',  'A');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('3',  'A');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('3',  'B');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('3',  'C');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('2',  'A');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('2',  'C');
insert into "org.apache.olingo.jpa::BusinessPartnerRole" values ('7',  'C');

CREATE TABLE "org.apache.olingo.jpa::Country" ( 
	"ISOCode" NVARCHAR(4) NOT NULL ,
	"LanguageISO" NVARCHAR(4) NOT NULL ,
	"Name" NVARCHAR(100) NOT NULL, 
     PRIMARY KEY ("ISOCode","LanguageISO"));
 
insert into "org.apache.olingo.jpa::Country" values( 'DEU','de','Deutschland');    
insert into "org.apache.olingo.jpa::Country" values( 'USA','de','Vereinigte Staaten von Amerika');   
insert into "org.apache.olingo.jpa::Country" values( 'DEU','en','Germany');
insert into "org.apache.olingo.jpa::Country" values( 'USA','en','United States of America');
insert into "org.apache.olingo.jpa::Country" values( 'BEL','de','Belgien');
insert into "org.apache.olingo.jpa::Country" values( 'BEL','en','Belgium');

CREATE TABLE "org.apache.olingo.jpa::Region"(
  "CountryISOCode" NVARCHAR(4) NOT NULL ,
  "RegionISOCode" NVARCHAR(10) NOT NULL,
  "LanguageISO" NVARCHAR(4) NOT NULL ,
  "Name" NVARCHAR(100) NOT NULL, 
     PRIMARY KEY ("CountryISOCode","RegionISOCode","LanguageISO")); 

insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BW','de','Baden-Württemberg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BY','de','Bayern Bayern');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BE','de','Berlin');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BB','de','Brandenburg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HB','de','Bremen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HH','de','Hamburg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HE','de','Hessen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-MV','de','Mecklenburg-Vorpommern');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-NI','de','Niedersachsen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-NW','de','Nordrhein-Westfalen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-RP','de','Rheinland-Pfalz');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SL','de','Saarland');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SN','de','Sachsen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-ST','de','Sachsen-Anhalt');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SH','de','Schleswig-Holstein');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-TH','de','Thüringen');  

insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BW','en','Baden-Württemberg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BY','en','Bavaria');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BE','en','Berlin');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-BB','en','Brandenburg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HB','en','Bremen');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HH','en','Hamburg');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-HE','en','Hesse');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-MV','en','Mecklenburg-Western Pomerania');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-NI','en','Lower Saxony');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-NW','en','North Rhine-Westphalia');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-RP','en','Rhineland-Palatinate');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SL','en','Saarland');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SN','en','Saxony');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-ST','en','Saxony-Anhalt');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-SH','en','Schleswig-Holstein');  
insert into "org.apache.olingo.jpa::Region" values( 'DEU','DE-TH','en','Thuringia');  

insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AL','de','Alabama');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AK','de','Alaska');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AZ','de','Arizona');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AR','de','Arkansas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CO','de','Colorado');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CT','de','Connecticut');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-DE','de','Delaware');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-DC','de','District of Columbia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-FL','de','Florida');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-GA','de','Georgia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-HI','de','Hawaii');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ID','de','Idaho');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IL','de','Illinois');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IN','de','Indiana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IA','de','Iowa');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CA','de','Kalifornien');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-KS','de','Kansas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-KY','de','Kentucky');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-LA','de','Louisiana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ME','de','Maine');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MD','de','Maryland');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MA','de','Massachusetts');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MI','de','Michigan');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MN','de','Minnesota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MS','de','Mississippi');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MO','de','Missouri');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MT','de','Montana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NE','de','Nebraska');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NV','de','Nevada');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NH','de','New Hampshire');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NJ','de','New Jersey');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NM','de','New Mexico');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NY','de','New York');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NC','de','North Carolina');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ND','de','North Dakota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OH','de','Ohio');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OK','de','Oklahoma');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OR','de','Oregon');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-PA','de','Pennsylvania');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-RI','de','Rhode Island');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-SC','de','South Carolina');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-SD','de','South Dakota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-TN','de','Tennessee');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-TX','de','Texas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-UT','de','Utah');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-VT','de','Vermont');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-VA','de','Virginia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WA','de','Washington');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WV','de','West Virginia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WI','de','Wisconsin');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WY','de','Wyoming');

insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AL','en','Alabama');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AK','en','Alaska');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AZ','en','Arizona');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-AR','en','Arkansas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CA','en','California');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CO','en','Colorado');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-CT','en','Connecticut');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-DE','en','Delaware');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-FL','en','Florida');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-GA','en','Georgia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-HI','en','Hawaii');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ID','en','Idaho');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IL','en','Illinois');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IN','en','Indiana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-IA','en','Iowa');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-KS','en','Kansas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-KY','en','Kentucky');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-LA','en','Louisiana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ME','en','Maine');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MD','en','Maryland');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MA','en','Massachusetts');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MI','en','Michigan');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MN','en','Minnesota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MS','en','Mississippi');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MO','en','Missouri');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-MT','en','Montana');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NE','en','Nebraska');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NV','en','Nevada');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NH','en','New Hampshire');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NJ','en','New Jersey');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NM','en','New Mexico');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NY','en','New York');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-NC','en','North Carolina');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-ND','en','North Dakota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OH','en','Ohio');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OK','en','Oklahoma');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-OR','en','Oregon');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-PA','en','Pennsylvania');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-RI','en','Rhode Island');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-SC','en','South Carolina');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-SD','en','South Dakota');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-TN','en','Tennessee');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-TX','en','Texas');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-UT','en','Utah');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-VT','en','Vermont');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-VA','en','Virginia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WA','en','Washington');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WV','en','West Virginia');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WI','en','Wisconsin');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-WY','en','Wyoming');
insert into "org.apache.olingo.jpa::Region" values( 'USA','US-DC','en','District of Columbia');


CREATE TABLE "org.apache.olingo.jpa::AdministrativeDivision"(
	"CodePublisher" NVARCHAR(10) NOT NULL,
	"CodeID" NVARCHAR(10) NOT NULL,
	"DivisionCode" NVARCHAR(10) NOT NULL,
	"CountryISOCode" NVARCHAR(4) NOT NULL ,
	"ParentCodeID" NVARCHAR(10),
	"ParentDivisionCode" NVARCHAR(10),
	"AlternativeCode" NVARCHAR(10),
	"Area" DECIMAL(34,0),
	"Population" BIGINT,
	PRIMARY KEY ("CodePublisher", "CodeID", "DivisionCode"));
	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-1','BEL','BEL',null,null,null,0,0);	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-1','DEU','DEU',null,null,null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-1','USA','USA',null,null,null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-BRU','BEL','3166-1','BEL',null,0,0);	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VLG','BEL','3166-1','BEL',null,0,0);	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WAL','BEL','3166-1','BEL',null,0,0);		
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VAN','BEL','3166-2','BE-VLG',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VLI','BEL','3166-2','BE-VLG',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VOV','BEL','3166-2','BE-VLG',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VBR','BEL','3166-2','BE-VLG',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-VWV','BEL','3166-2','BE-VLG',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WBR','BEL','3166-2','BE-WAL',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WHT','BEL','3166-2','BE-WAL',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WLG','BEL','3166-2','BE-WAL',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WLX','BEL','3166-2','BE-WAL',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2','BE-WNA','BEL','3166-2','BE-WAL',null,0,0);	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-BW','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-BY','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-BE','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-BB','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-HB','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-HH','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-HE','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-MV','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-NI','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-NW','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-RP','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-SL','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-SN','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-ST','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-SH','DEU', '3166-1','DEU',null,0,0);  
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'DE-TH','DEU', '3166-1','DEU',null,0,0); 
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'ISO', '3166-2', 'US-CA','USA', '3166-1','USA',null,0,0); 
--Eurostat 
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS1','BE1', 'BEL',null,null,null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS1','BE2', 'BEL',null,null,null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS1','BE3', 'BEL',null,null,null,0,0);

insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE10','BEL','NUTS1','BE1',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE21','BEL','NUTS1','BE2',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE22','BEL','NUTS1','BE2',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE23','BEL','NUTS1','BE2',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE24','BEL','NUTS1','BE2',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE25','BEL','NUTS1','BE2',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE31','BEL','NUTS1','BE3',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE32','BEL','NUTS1','BE3',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE33','BEL','NUTS1','BE3',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE34','BEL','NUTS1','BE3',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS2','BE35','BEL','NUTS1','BE3',null,0,0);

insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE100','BEL','NUTS2','BE10',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE211','BEL','NUTS2','BE21',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE212','BEL','NUTS2','BE21',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE213','BEL','NUTS2','BE21',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE221','BEL','NUTS2','BE22',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE222','BEL','NUTS2','BE22',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE223','BEL','NUTS2','BE22',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE231','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE232','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE233','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE234','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE235','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE236','BEL','NUTS2','BE23',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE241','BEL','NUTS2','BE24',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE242','BEL','NUTS2','BE24',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE251','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE252','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE253','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE254','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE255','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE256','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE257','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE258','BEL','NUTS2','BE25',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE310','BEL','NUTS2','BE31',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE321','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE322','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE323','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE324','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE325','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE326','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE327','BEL','NUTS2','BE32',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE331','BEL','NUTS2','BE33',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE332','BEL','NUTS2','BE33',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE334','BEL','NUTS2','BE33',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE335','BEL','NUTS2','BE33',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE336','BEL','NUTS2','BE33',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE341','BEL','NUTS2','BE34',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE342','BEL','NUTS2','BE34',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE343','BEL','NUTS2','BE34',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE344','BEL','NUTS2','BE34',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE345','BEL','NUTS2','BE34',null,0,0);	
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE351','BEL','NUTS2','BE35',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE352','BEL','NUTS2','BE35',null,0,0);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat','NUTS3','BE353','BEL','NUTS2','BE35',null,0,0);


insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33011','BEL','NUTS3','BE253',null,130610415,35098);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33016','BEL','NUTS3','BE253',null,3578335,1037);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33021','BEL','NUTS3','BE253',null,119330610,19968);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33029','BEL','NUTS3','BE253',null,43612479,18456);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33037','BEL','NUTS3','BE253',null,67573324,12400);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33039','BEL','NUTS3','BE253',null,94235304,7888);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33040','BEL','NUTS3','BE253',null,52529046,8144);
insert into "org.apache.olingo.jpa::AdministrativeDivision" values( 'Eurostat', 'LAU2', '33041','BEL','NUTS3','BE253',null,38148241,3625);
