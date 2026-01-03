package com.example.chat.storage.config.db.datasource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "spring.datasource")
public class MultiDataSourceProperties {

	private final Source source = new Source();
	private final Replica replica = new Replica();

	@Setter
	@Getter
	public static class Source {
		private String driverClassName;
		private String url;
		private String username;
		private String password;

		public DataSourceProperties toDataSourceProperties() {
			DataSourceProperties props = new DataSourceProperties();
			props.setDriverClassName(driverClassName);
			props.setUrl(url);
			props.setUsername(username);
			props.setPassword(password);
			return props;
		}
	}

	@Setter
	@Getter
	public static class Replica {
		private String driverClassName;
		private String url;
		private String username;
		private String password;

		public DataSourceProperties toDataSourceProperties() {
			DataSourceProperties props = new DataSourceProperties();
			props.setDriverClassName(driverClassName);
			props.setUrl(url);
			props.setUsername(username);
			props.setPassword(password);
			return props;
		}
	}
}