package GB;

import Pipes.Consumer;
import Pipes.Message;
import Pipes.Pipe;
import Pipes.Producer;
import Pipes.Tester;
import Pipes.Transformer;

import java.util.Random;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

import GB.Note;

public class GB 
{
	int numCalls = 0;
	
	Random rand;
	
	Producer<Note> digitalComposer;
	Tester<Note, Boolean> noiseFilter1, noiseFilter2;
	Transformer<Note, Note> amplifier;
	Consumer<Note> player;
	
	public GB() 
	{
		rand = new Random();
		
		digitalComposer = new Producer<Note>();
		digitalComposer.produce = () -> 
		{
			/*
			 * 		PRODUCER
			 */
			try
			{
				//System.out.println("Produce method");
				if (numCalls > 100) 
				{
					//System.out.println("Produce method if part");
					throw new Exception("I'm done");
				}
				else
				{
					//System.out.println("Produce method else part");
					Note n = new Note(rand.nextInt(127), 0, rand.nextInt(300));
					//System.out.println("numcals = " + numCalls);
					numCalls++;
					return n;
				}
			}
			catch(Exception e)
			{
				//System.out.println("Exception");
				return null;
			}
		};
		
		amplifier = new Transformer<Note, Note>();
		amplifier.transform = (Note n) ->
		{
			/*
			 * 		TRANSFORMER
			 */
			try
			{
				//System.out.println("amplifier");
				if(n.frequency %2 == 0)
					n.setAmplitude(55);
				else
					n.setAmplitude(60);
				return n;
			}
			catch(Exception e)
			{
				//System.out.println(e);
				return null;
			}
		};
		
		noiseFilter1 = new Tester<Note, Boolean>();
		noiseFilter1.test = (Note n) ->
		{
			/*
			 * 		TESTER 1
			 */
			try
			{
				//System.out.println("noise filter 1");
				if(n.frequency %2 == 0)
					return n;
				else
					return null;
			}
			catch(Exception e)
			{
				//System.out.println(e);
				return null;
			}
		};
		
		noiseFilter2 = new Tester<Note, Boolean>();
		noiseFilter2.test = (Note n) ->
		{
			/*
			 * 		TESTER 2
			 */
			try
			{
				//System.out.println("noise filter 2");
				if(n.duration > 50)
					return n;
				else
					return null;
			}
			catch(Exception e)
			{
				//System.out.println(e);
				return null;
			}
		};
		
		player = new Consumer<Note>();
		player.consume = (Note n) ->
		{
			/*
			 * 		CONSUMER
			 */
			try
			{
				//System.out.println("Consumer");
				int channel = 0;
				Synthesizer synthesizer = MidiSystem.getSynthesizer();
				synthesizer.open();
				MidiChannel channels[] = synthesizer.getChannels();
				
				channels[channel].noteOn(n.frequency, n.amplitude);
				Thread.sleep(n.duration);
				channels[channel].noteOff(n.frequency);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		};
	}
	
	public void buildDataDrivenPipeLine()
	{
		Pipe pipe1 = new Pipe<>();
		Pipe pipe2 = new Pipe<>();
		Pipe pipe3 = new Pipe<>();
		Pipe pipe4 = new Pipe<>();
		
		Pipe.isDataDriven = true;
		
		/*
		 * 		Building data driven pipeline
		 */
		digitalComposer.setOut(pipe1);
		
		noiseFilter1.setIn(pipe1);
		noiseFilter1.setOut(pipe2);
		
		noiseFilter2.setIn(pipe2);
		noiseFilter2.setOut(pipe3);
		
		amplifier.setIn(pipe3);
		amplifier.setOut(pipe4);
		
		player.setIn(pipe4);
		
		digitalComposer.start();
		
	}
	
	public void builDemandDrivenPipeLine()
	{
		Pipe pipe1 = new Pipe<>();
		Pipe pipe2 = new Pipe<>();
		Pipe pipe3 = new Pipe<>();
		Pipe pipe4 = new Pipe<>();
		
		Pipe.isDataDriven = false;
		
		/*
		 * 		Building demand driven pipeline
		 */
		digitalComposer.setOut(pipe1);
		
		noiseFilter1.setIn(pipe1);
		noiseFilter1.setOut(pipe2);
		
		noiseFilter2.setIn(pipe2);
		noiseFilter2.setOut(pipe3);
		
		amplifier.setIn(pipe3);
		amplifier.setOut(pipe4);
		
		player.setIn(pipe4);
		
		player.start();
		
	}
	
	public static void main(String[] args) 
	{
		GB gb = new GB();
		//gb.buildDataDrivenPipeLine();
		gb.builDemandDrivenPipeLine();
	}
}
