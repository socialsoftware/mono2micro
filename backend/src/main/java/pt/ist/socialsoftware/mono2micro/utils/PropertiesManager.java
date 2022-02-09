package pt.ist.socialsoftware.mono2micro.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:application.properties", "classpath:specific.properties" })
public class PropertiesManager {

	@Value("${scripts.address}")
	private String scriptsAddress;

	@Value("${codebases.path}")
	private String codebasesPath;

	@Bean(name="scriptsAddress")
	public String getScriptsAddress() {
		return scriptsAddress;
	}

	@Bean(name="codebasesPath")
	public String getCodebasesPath() {
		return codebasesPath;
	}
}