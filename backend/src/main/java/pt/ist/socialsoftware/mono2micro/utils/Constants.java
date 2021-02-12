package pt.ist.socialsoftware.mono2micro.utils;

import org.springframework.beans.factory.annotation.Value;

public final class Constants {
    
  private Constants(){
  }

  public static String SCRIPTS_PATH = PropertiesManager.getProperties().getProperty("scripts.path");
  public static String CODEBASES_PATH = PropertiesManager.getProperties().getProperty("codebases.path");
  public static String PYTHON = PropertiesManager.getProperties().getProperty("python");

  enum Mode {
    READ,
    WRITE,
    READWRITE
  }
}