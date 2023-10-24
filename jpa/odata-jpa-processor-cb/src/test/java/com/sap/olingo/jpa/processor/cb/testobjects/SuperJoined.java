package com.sap.olingo.jpa.processor.cb.testobjects;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity(name = "Super")
@Inheritance(strategy = InheritanceType.JOINED)
public class SuperJoined {

}
