package pt.ist.socialsoftware.mono2micro.functionality.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.util.*;

import org.json.JSONObject;

public class FieldDto {
	protected String name;
	protected String type;
	protected Boolean isList = false;

	public FieldDto() {}

	@JsonCreator
	public FieldDto(
		@JsonProperty("name") String name,
		@JsonProperty("parameter") String type,
		@JsonProperty("isList") Boolean isList
	) {
		this.name = name;
		this.type = type;
		this.isList = isList;
	}

	@JsonProperty("name")
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }

	@JsonProperty("type")
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	@JsonProperty("isList")
	public Boolean getIsList() { return isList; }
	public void setIsList(Boolean isList) { this.isList = isList; }
}
