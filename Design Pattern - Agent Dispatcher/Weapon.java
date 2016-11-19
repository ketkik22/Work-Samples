package ud;

public abstract class Weapon 
{
	protected Gladiator owner;
	
	public Strike makeStrike()
	{
		return null;
	}

	public Gladiator getOwner() 
	{
		return owner;
	}

	public void setOwner(Gladiator owner) 
	{
		this.owner = owner;
	}
}

class Wand extends Weapon
{
	@Override
	public Strike makeStrike() 
	{
		// TODO Auto-generated method stub
		return new Strike(10, "befuddling spell", StrikeType.Magic);
	}
}

class Poison extends Weapon
{
	@Override
	public Strike makeStrike() 
	{
		// TODO Auto-generated method stub
		return new Strike(15, "bottle of stinky poison", StrikeType.Chemical);
	}
}

class Sword extends Weapon
{
	@Override
	public Strike makeStrike() 
	{
		// TODO Auto-generated method stub
		return new Strike(25, "shiny sharp sword", StrikeType.Iron);
	}
}

class FlameThrower extends Weapon
{
	@Override
	public Strike makeStrike() 
	{
		// TODO Auto-generated method stub
		return new Strike(5, "napalm dripping flame thrower", StrikeType.Fire);
	}
}