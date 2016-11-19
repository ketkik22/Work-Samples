package ud;

public class BasicShield implements Shield 
{
	@Override
	public Strike reduceStrike(Strike s) 
	{
		// TODO Auto-generated method stub
		int strength = s.getStrength() - 5;
		s.setStrength(strength);
		return s;
	}
}
