package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;

@Embeddable
public class InhouseAddressWithThreeProtections {

  @Column(name = "\"Task\"", length = 32, nullable = false) // Workaround olingo problem
  private String taskID;

  @Column(name = "\"Building\"", length = 10)
  @EdmProtectedBy(name = "BuildingNumber", wildcardSupported = false)
  private String building;

  @Column(name = "\"Floor\"")
  @EdmProtectedBy(name = "Floor", wildcardSupported = false)
  private Short floor;

  @Column(name = "\"RoomNumber\"")
  @EdmProtectedBy(name = "RoomNumber")
  private Integer roomNumber;

  public InhouseAddressWithThreeProtections() {
    // Needed by JPA
  }

  public InhouseAddressWithThreeProtections(final String taskID, final String building) {
    this.setTaskID(taskID);
    this.setBuilding(building);
  }

  public String getBuilding() {
    return building;
  }

  public void setBuilding(final String building) {
    this.building = building;
  }

  public Short getFloor() {
    return floor;
  }

  public void setFloor(final Short floor) {
    this.floor = floor;
  }

  public Integer getRoomNumber() {
    return roomNumber;
  }

  public void setRoomNumber(final Integer roomNumber) {
    this.roomNumber = roomNumber;
  }

  public String getTaskID() {
    return taskID;
  }

  public void setTaskID(final String taskID) {
    this.taskID = taskID;
  }

}
