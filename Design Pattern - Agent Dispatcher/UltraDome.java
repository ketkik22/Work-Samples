package ud;

import java.util.LinkedList;
import java.util.Random;

import agency.Agent;
import agency.Dispatcher;

public class UltraDome extends Dispatcher
{
	LinkedList<Weapon> weapons;
	LinkedList<ShieldSkin> shieldSkins;
	private int MedicineQuantity;
	
	
	public UltraDome(int n, int quan, int noOfWeapons, int noOfShieldSkins) 
	{
		// TODO Auto-generated constructor stub
		super(n);
		MedicineQuantity = quan;
		
		weapons = new LinkedList<Weapon>();
		for(int i=0; i<noOfWeapons/4; i++)
		{
			Weapon w = new Wand();
			weapons.add(w);
			
			w = new Sword();
			weapons.add(w);
			
			w = new Poison();
			weapons.add(w);
			
			w = new FlameThrower();
			weapons.add(w);
		}
		
		shieldSkins = new LinkedList<ShieldSkin>();
		for(int i=0; i<noOfShieldSkins/4; i++)
		{
			ShieldSkin ss = new FireResistent();
			shieldSkins.add(ss);
			
			ss = new MetalResistent();
			shieldSkins.add(ss);
			
			ss = new MagicResistent();
			shieldSkins.add(ss);
			
			ss = new ChemResistent();
			shieldSkins.add(ss);
		}
	}
	
	@Override
	public Agent makeAgent() 
	{
		// TODO Auto-generated method stub
		return new Gladiator();
	}

	public void addWeapon(Weapon w)
	{
		weapons.add(w);
	}
	
	public void addShieldSkin(ShieldSkin s)
	{
		shieldSkins.add(s);
	}

	public int getMedicineQuantity() 
	{
		return MedicineQuantity;
	}
	
	public void setMedicineQuantity(int medicineQuantity) 
	{
		MedicineQuantity = medicineQuantity;
	}

	public synchronized Weapon getWeapon()
	{
		Weapon w;
		if(weapons.size() == 0)
		{
			w = null;
		}
		else
		{
			Random r = new Random();
			int index = r.nextInt(weapons.size());
			w = weapons.remove(index);
		}
		return w;
	}
	
	public synchronized ShieldSkin getShieldSkin()
	{
		ShieldSkin s;
		if(shieldSkins.size() == 0)
		{
			s = null;
		}
		else
		{
			Random r = new Random();
			int index = r.nextInt(shieldSkins.size());
			s = shieldSkins.remove(index);
		}
		return s;
	}
	
	public static void main(String[] args) 
	{
		UltraDome ud = new UltraDome(3, 200, 8, 8);
		ud.run();
	}
}