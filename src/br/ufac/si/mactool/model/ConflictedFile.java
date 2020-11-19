package br.ufac.si.mactool.model;

public class ConflictedFile 
{
	/**
	 * Name of the file in conflict 
	 * */
	private String fileName;
	
	/**
	 * Name of the authors who caused conflict
	 * */
	private String authors[];
	
	/**
	 * Hash of commits where the conflict has been caused
	 * */
	private String commits[];
	
	/**
	 * If the file was deleted before the merge
	 * */
	private boolean deleted;
	
	public ConflictedFile(String nome)
	{
		this.fileName = nome;
		authors = new String[2];
		commits = new String[2];
		deleted = false;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String[] getAuthors() {
		return authors;
	}

	public void setAuthors(String[] authors) {
		this.authors = authors;
	}
	
	public String getAuthor(int index) {
		return this.authors[index];
	}
	
	public void setAuthor(int index, String author) {
		this.authors[index] = author;
	}

	public String[] getCommits() {
		return commits;
	}

	public void setCommits(String[] commits) {
		this.commits = commits;
	}
	
	public String getCommit(int index) {
		return this.commits[index];
	}
	
	public void setCommit(int index, String author) {
		this.commits[index] = author;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
