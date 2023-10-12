package com.sap.olingo.jpa.processor.core.testmodel;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

//Problem with multi-level inheritance hierarchy Inheritance Type SINGLE_TABLE. Therefore inherit also from
//Business Partner
@Entity(name = "CurrentUser")
@DiscriminatorValue(value = "1")
@EdmEntityType(as = EdmTopLevelElementRepresentation.AS_SINGLETON_ONLY,
    extensionProvider = CurrentUserQueryExtension.class)
public class CurrentUser extends Person {}
