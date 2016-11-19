package ud;

public class Strike 
{
	private int strength;
	private String desctiption;
	private StrikeType type;
	
	public Strike(int strength, String description, StrikeType type)
	{
		// TODO Auto-generated constructor stub
		this.strength = strength;
		this.desctiption = description;
		this.type = type;
	}
	
	public int getStrength() 
	{
		return strength;
	}
	public void setStrength(int strength) 
	{
		this.strength = strength;
	}
	public String getDesctiption() 
	{
		return desctiption;
	}
	public void setDesctiption(String desctiption) 
	{
		this.desctiption = desctiption;
	}
	public StrikeType getType() 
	{
		return type;
	}
	public void setType(StrikeType type) 
	{
		this.type = type;
	}
}
