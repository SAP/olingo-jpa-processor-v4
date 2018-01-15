package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;

//Problem with multi-level inheritance hierarchy Inheritance Type SINGLE_TABLE. Therefore inherit also from 
//Business Partner
@Entity(name = "BestOrganization")
@DiscriminatorValue(value = "2")
@EdmAsEntitySet
public class BestOrganization extends BusinessPartner {}
