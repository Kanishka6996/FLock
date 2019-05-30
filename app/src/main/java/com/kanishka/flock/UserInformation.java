package com.kanishka.flock;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInformation {

  public String firstName;
  public String lastName;
  public String mobile;
  public HashMap<String, String> map;
  public ArrayList<String> users;

  public UserInformation() {

  }

  public UserInformation(String fName, String lName, String mobile) {
    this.firstName = fName;
    this.lastName = lName;
    this.mobile = mobile;
  }

  public UserInformation(ArrayList<String> users) {
    this.users = users;
  }

  public UserInformation(HashMap<String, String> map) {
    this.map = map;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public HashMap<String, String> getMap() {
    return map;
  }

  public void setMap(HashMap<String, String> map) {
    this.map = map;
  }

  public ArrayList<String> getUsers() {
    return users;
  }

  public void setUsers(ArrayList<String> users) {
    this.users = users;
  }
}

