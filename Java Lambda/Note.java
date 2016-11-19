package GB;

public class Note
{
	int frequency;
	int amplitude;
	int duration;
	
	public Note(int frequency, int amplitude, int duration)
	{
		this.frequency = frequency;
		this.amplitude = amplitude;
		this.duration = duration;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(int amplitude) {
		this.amplitude = amplitude;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}
