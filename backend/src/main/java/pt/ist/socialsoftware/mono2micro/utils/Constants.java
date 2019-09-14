package pt.ist.socialsoftware.mono2micro.utils;

public final class Constants {
    
    private Constants(){

    }
    
    public static String CODEBASES_FOLDER = "src/main/resources/codebases/";
    public static String RESOURCES_PATH = "src/main/resources/";
    public static String PYTHON = "python";

    enum Mode {
		READ,
		WRITE,
		READWRITE
	}
}