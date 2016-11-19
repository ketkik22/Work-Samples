package agency;

import java.util.*;

public abstract class Dispatcher 
{
	private List<Agent> agents;
	private Random generator;

	// many simulations require some randomness
	public synchronized int nextInt(int ub) 
	{
		return generator.nextInt(ub);
	}

	public Dispatcher(int numAgents) 
	{
		generator = new Random(System.nanoTime());
		agents = new ArrayList<Agent>();
		for(int n = 0; n < numAgents; n++) 
		{
			Agent next = makeAgent();
			next.setID(n);
			next.setDispatcher(this);
			agents.add(next);
		}
	}

	public synchronized Agent getPartner(Agent initiator) 
	{
		if (agents.size() <= 1) 
		{
			return null;
		}
		int start = nextInt(agents.size());
		int index = start;
		Agent partner = agents.get(index);
		while (partner.isDead() || partner == initiator) 
		{
			index = (index + 1) % agents.size();
			partner = agents.get(index);
			if (index == start) 
			{
				partner = null;
				break;
			}
		}
		return partner;
	}

	// abstract factory method
	public abstract Agent makeAgent();

	public void run() 
	{
		for(Agent agent: agents) 
		{
			agent.start();
		}
		for(Agent agent: agents) 
		{
			try 
			{
				agent.join();
			} 
			catch(InterruptedException ie) 
			{
				System.err.println(ie.getMessage());
			} 
			finally 
			{
				System.out.println("" + agent + " has terminated");
			}
		}
		System.out.print("done");
	}
}
