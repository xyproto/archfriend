
package com.xyproto.archfriend.model;

public class Package {

  private String name;
  private String version;

  public Package(String name, String version) {
    super();
    this.name = name;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return name + " " + version;
  }

}
