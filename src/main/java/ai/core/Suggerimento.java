package ai.core;

public class Suggerimento {
	
	private String name;
	private String resid;
	private String parentId;
	private String parentName;
	private String newSugg;
	
	public Suggerimento(){}

	public String getName() {
		return name;
	}
	public String getResid() {
		return resid;
	}
	public String getParentId() {
		return parentId;
	}
	public String getParentName() {
		return parentName;
	}
	public String newSugg() {
		return newSugg;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	public void setResid(String resid)
	{
		this.resid = resid;
	}
	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}
	public void setParentName(String parentName)
	{
		this.parentName = parentName;
	}
	public void setNewSugg(String newSugg)
	{
		this.newSugg = newSugg;
	}
}