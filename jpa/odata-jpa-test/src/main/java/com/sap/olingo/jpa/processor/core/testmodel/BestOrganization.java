package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;

//Problem with multi-level inheritance hierarchy Inheritance Type SINGLE_TABLE. Therefore inherit also from
//Business Partner
@Entity(name = "BestOrganization")
@DiscriminatorValue(value = "2")
@EdmEntityType(as = EdmTopLevelElementRepresentation.AS_ENTITY_SET_ONLY)
public class BestOrganization extends BusinessPartner {}
