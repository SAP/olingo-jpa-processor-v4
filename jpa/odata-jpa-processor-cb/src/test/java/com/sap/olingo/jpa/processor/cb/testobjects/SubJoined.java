package com.sap.olingo.jpa.processor.cb.testobjects;

import javax.persistence.PrimaryKeyJoinColumn;

@PrimaryKeyJoinColumn(name = "\"Key1\"", referencedColumnName = "\"Value1\"")
public class SubJoined extends SuperJoined {

}
