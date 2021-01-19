package br.ufac.si.mactool.model;

public class ConflictCommitter {

	/**
	 * Developer name
	 * */
	private String name;
	
	/**
	 * Number of conflicts
	 * */
	private Integer conflicts;

	/**
	 * Number of self conflicts
	 * */
	private Integer selfConflicts;

	public ConflictCommitter(String name, Integer conflicts, Integer selfConflicts) {
		super();
		this.name = name;
		this.conflicts = conflicts;
		this.selfConflicts = selfConflicts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getConflicts() {
		return conflicts;
	}

	public void setConflicts(Integer conflicts) {
		this.conflicts = conflicts;
	}

	public Integer getSelfConflicts() {
		return selfConflicts;
	}

	public void setSelfConflicts(Integer selfConflicts) {
		this.selfConflicts = selfConflicts;
	}
}
