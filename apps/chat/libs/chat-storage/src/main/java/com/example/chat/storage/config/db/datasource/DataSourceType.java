package com.example.chat.storage.config.db.datasource;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataSourceType {
	SOURCE,
	REPLICA,
	;

	private final String key = name().toLowerCase();
}