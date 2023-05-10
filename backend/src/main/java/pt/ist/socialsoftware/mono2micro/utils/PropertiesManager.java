package pt.ist.socialsoftware.mono2micro.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
		@PropertySource("classpath:application.properties"),
		@PropertySource("classpath:specific.properties")
})
public class PropertiesManager {

	@Value("${scripts.address}")
	private String scriptsAddress;

	@Value("${export.path}")
	private String exportPath;

	@Bean(name="scriptsAddress")
	public String getScriptsAddress() {
		return scriptsAddress;
	}

	@Bean(name="exportPath")
	public String getExportPath() { return exportPath; }
}