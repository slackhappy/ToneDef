package edu.columbia.cs.jmg.ToneDef;



public class ToneStream
{
	Thread loop;
	boolean started;
	//final LinkedBlockingQueue<Integer> toneq; 

	volatile Integer tone;
	
	
	public ToneStream()
	{
		started = false;
		tone = 0;
	}
	
	public void start()
	{
		tone = 0;
		loop = new Thread(new Runnable()
		{
			public void run()
			{
				AndroidAudioDevice device = new AndroidAudioDevice();
				float freq = 0;
				float increment = 0;
				float angle = 0;
				float samples[] = new float[1024];
				
				
				while (true) 
				{
					Integer headfreq = null;
					/*
					if(freq > 0)  //if there is something playing, do non-blocking version
					{
						headfreq = toneq.poll();
						
					}
					else //else do blocking version
					{
						try 
						{
							headfreq = toneq.take();
						} 
						catch (InterruptedException e) 
						{
							break;
						}
					}*/
					headfreq = tone;
					
					if(headfreq != null)
					{
						freq = headfreq;
						increment = getIncrement(freq);
					}
					
					if(freq > 0)
					{
					
						for (int i = 0; i < samples.length; i++) {
							samples[i] = (float) Math.sin(angle);
							angle += increment;
						}
		
						device.writeSamples(samples);
					}
					else
					{
						try 
						{
							Thread.sleep(100);
						} 
						catch (InterruptedException e) 
						{
							break;
						}
					}
					
					if(freq < 0)
					{
						device.stop();
						break;
					}
				}
			}
		});
		loop.start();
		started = true;
	}
	
	public void stop()
	{
		tone = -1;
		try {
			loop.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		started = false;
	}
	
	public void add(int freq)
	{
		//toneq.add(freq);
		tone = freq;
	}
	
	public boolean isStarted()
	{
		return started;
	}
	
	public static float getIncrement(float frequency)
	{
		return  (float) (2 * Math.PI) * frequency / 44100; // angular increment  for each sample
	}
	
}
