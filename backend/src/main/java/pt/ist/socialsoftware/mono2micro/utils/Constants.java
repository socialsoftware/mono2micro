package pt.ist.socialsoftware.mono2micro.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public final class Constants {
    
  private Constants(){
  }

  static ApplicationContext context = new AnnotationConfigApplicationContext(PropertiesManager.class);

  public static String SCRIPTS_ADDRESS = (String) context.getBean("scriptsAddress");
  public static String EXPORT_PATH = (String) context.getBean("exportPath");
  public static String MOJO_RESOURCES_PATH = "src/main/java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/" +
          "src/main/resources/";
  public static String STRATEGIES_FOLDER = "/strategies/";
  public static String RECOMMEND_FOLDER = "/recommendStrategies/";
  public static final String DEFAULT_REDESIGN_NAME = "Monolith Trace";

  public enum TraceType {
    ALL,
    LONGEST,
    WITH_MORE_DIFFERENT_ACCESSES,
  }

  enum Mode {
    READ,
    WRITE,
    READWRITE
  }
}