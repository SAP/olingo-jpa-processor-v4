package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InhouseAddress {

  @Column(name = "\"Task\"", length = 32, nullable = false) // Workaround olingo problem
  private String taskID;
  @Column(name = "\"Building\"", length = 10)
  private String building;
  @Column(name = "\"Floor\"")
  private Short floor;
  @Column(name = "\"RoomNumber\"")
  private Integer roomNumber;

  public InhouseAddress() {
    // Needed by JPA
  }

  public InhouseAddress(final String taskID, final String building) {
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

  @Override
  public String toString() {
    return "InhouseAddress [taskID=" + taskID + ", building=" + building + ", floor=" + floor + ", roomNumber="
        + roomNumber + "]";
  }

}
