package ud;

import java.util.Random;

import agency.Agent;

public class Gladiator extends Agent
{
	private int health;
	private Weapon weapon;
	private Shield shield;
	
	public Gladiator() 
	{
		health = 100;
		weapon = new Sword();
		shield = new BasicShield();
	}

	@Override
	public void interact(Agent other) 
	{
		// TODO Auto-generated method stub
		System.out.println("Gladiator." + getID() + " is about to attack Gladiator." + ((Gladiator)other).getID());
		Strike s = weapon.makeStrike();
		((Gladiator)other).defend(s);
	}
	
	public void defend(Strike s)
	{
		s = shield.reduceStrike(s);
		System.out.println("Gladiator." + getID() + " is defending against a " + s.getDesctiption() + " of strength " + s.getStrength());
		this.health = this.health - s.getStrength();
		if(this.health <= 0)
		{
			this.health = 0;
			this.die();
		}
		System.out.println("Gladiator." + getID() + ".health = " + this.health);
	}
	
	public void strengthenShield(ShieldSkin ss)
	{
		System.out.println("Gladiator." + getID() + " is strengthening his/her shield with a " + this.shield.getClass().getName() + " skin.");
		ss.setPeer(this.shield);
		setShield(ss);
	}
	
	@Override
	public void update() 
	{
		if(isDead())
		{
			System.out.println("Gladiator." + getID() + " has died.");
		}
		else
		{
			Random r = new Random();
			int luck = r.nextInt(100);
			
			if(0 <= luck && luck < 10)
			{
				int medicineQuantity = ((UltraDome)getDispatcher()).getMedicineQuantity();
				
				while(this.health < 100 && medicineQuantity > 0)
				{
					this.health++;
					medicineQuantity--;
				}
				System.out.println("Gladiator." + getID() + " is taking a medicine.");
				System.out.println("Remaining medicine = " + medicineQuantity);
				System.out.println("Gladiator." + getID() + ".health = " + this.health);
				((UltraDome)getDispatcher()).setMedicineQuantity(medicineQuantity);
			}
			else if(10 <= luck && luck < 20)
			{
				Weapon w = ((UltraDome)getDispatcher()).getWeapon();
				if(w != null)
				{
					setWeapon(w);
				}
				Strike s = weapon.makeStrike();
				System.out.println("Gladiator." + getID() + " is picking up a " + s.getDesctiption());
			}
			else if(20 <= luck && luck < 30)
			{
				Shield s = ((UltraDome)getDispatcher()).getShieldSkin();
				if(s != null)
				{
					setShield(s);
				}
			}
			else
			{
				super.update();
			}
		}
	}

	public int getHealth() 
	{
		return health;
	}

	public void setHealth(int health) 
	{
		this.health = health;
	}

	public Weapon getWeapon() 
	{
		return weapon;
	}

	public void setWeapon(Weapon weapon) 
	{
		this.weapon = weapon;
	}

	public Shield getShield() 
	{
		return shield;
	}

	public void setShield(Shield shield) 
	{
		this.shield = shield;
	}
}
