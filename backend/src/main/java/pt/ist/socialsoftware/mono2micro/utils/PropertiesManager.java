package pt.ist.socialsoftware.mono2micro.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:application.properties", "classpath:specific.properties" })
public class PropertiesManager {

	@Value("${scripts.path}")
	private String scriptsPath;

	@Value("${scripts.address}")
	private String scriptsAddress;

	@Value("${codebases.path}")
	private String codebasesPath;

	@Value("${python}")
	private String python;

	@Bean(name="scriptsPath")
	public String getScriptsPath() {
		return scriptsPath;
	}

	@Bean(name="scriptsAddress")
	public String getScriptsAddress() {
		return scriptsAddress;
	}

	@Bean(name="codebasesPath")
	public String getCodebasesPath() {
		return codebasesPath;
	}

	@Bean(name="python")
	public String getPython() {
		return python;
	}
}