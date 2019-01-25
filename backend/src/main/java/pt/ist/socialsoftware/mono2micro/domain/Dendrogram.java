package pt.ist.socialsoftware.mono2micro.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dendrogram {
	private String name;
	private File datafile;

	public Dendrogram(String name, File datafile) {
        this.name = name;
        this.datafile = datafile;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
