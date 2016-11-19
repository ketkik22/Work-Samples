package agency;

public abstract class Agent extends Thread 
{
	protected int id;
	protected Dispatcher dispatcher;
	protected String description;
	protected boolean dead;

	public Agent(String description) 
	{
		this.description = description;
		dead = false;
	}
	public Agent() 
	{
		this("Agent");
	}

	public boolean isDead() 
	{ 
		return dead; 
	}

	public void die() 
	{ 
		dead = true; 
	}

	public int getID() 
	{
		return id;
	}

	public void setID(int id) 
	{
		this.id = id;
	}

	public Dispatcher getDispatcher() 
	{
		return dispatcher;
	}

	public void setDispatcher(Dispatcher dispatcher) 
	{
		this.dispatcher = dispatcher;
	}

	public void update() 
	{
		Agent partner = dispatcher.getPartner(this);
		if (partner == null) 
		{
			System.out.println("Last agent standing: " + this);
			dead = true;
		} 
		else 
		{
			interact(partner);
		}

	}

	// to be specified by customizations:
	abstract public void interact(Agent other);


	public void run() 
	{
		while(!dead) 
		{
			update();
			yield();
		}
		System.out.println(this.toString() + " has died");
	}

	@Override
	public String toString() 
	{
		return description + "." + id;
	}
}
