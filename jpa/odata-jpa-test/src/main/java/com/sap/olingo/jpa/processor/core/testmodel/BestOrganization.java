package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;

@Entity(name = "BestOrganization")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
@DiscriminatorValue(value = "3")
@EdmAsEntitySet
public class BestOrganization extends BusinessPartner {}
