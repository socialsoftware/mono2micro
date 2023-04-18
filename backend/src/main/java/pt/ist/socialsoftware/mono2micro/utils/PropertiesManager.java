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

	@Bean(name="scriptsAddress")
	public String getScriptsAddress() {
		return scriptsAddress;
	}
}