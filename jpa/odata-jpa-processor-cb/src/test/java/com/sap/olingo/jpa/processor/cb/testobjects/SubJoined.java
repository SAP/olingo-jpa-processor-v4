package com.sap.olingo.jpa.processor.cb.testobjects;

import jakarta.persistence.PrimaryKeyJoinColumn;

@PrimaryKeyJoinColumn(name = "\"Key1\"", referencedColumnName = "\"Value1\"")
public class SubJoined extends SuperJoined {

}
