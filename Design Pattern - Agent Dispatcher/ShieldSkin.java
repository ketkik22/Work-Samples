package ud;

public class ShieldSkin implements Shield 
{
	Shield peer;
	
	@Override
	public Strike reduceStrike(Strike s)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Shield getPeer() 
	{
		return peer;
	}

	public void setPeer(Shield peer) 
	{
		this.peer = peer;
	}
}

class MagicResistent extends ShieldSkin
{
	@Override
	public Strike reduceStrike(Strike s) 
	{
		// TODO Auto-generated method stub
		if(s.getType().equals(StrikeType.Magic))
		{
			int strength = s.getStrength() - 20;
			s.setStrength(strength);
		}
		
		return s;
		
	}
}

class ChemResistent extends ShieldSkin
{
	@Override
	public Strike reduceStrike(Strike s) 
	{
		// TODO Auto-generated method stub
		if(s.getType().equals(StrikeType.Chemical))
		{
			int strength = s.getStrength() - 15;
			s.setStrength(strength);
		}
		
		return s;
	}
}

class MetalResistent extends ShieldSkin
{
	@Override
	public Strike reduceStrike(Strike s) 
	{
		// TODO Auto-generated method stub
		if(s.getType().equals(StrikeType.Iron))
		{
			int strength = s.getStrength() - 10;
			s.setStrength(strength);
		}
		
		return s;
	}
}

class FireResistent extends ShieldSkin
{
	@Override
	public Strike reduceStrike(Strike s) 
	{
		// TODO Auto-generated method stub
		if(s.getType().equals(StrikeType.Fire))
		{
			int strength = s.getStrength() - 5;
			s.setStrength(strength);
		}
		
		return s;
	}
}
