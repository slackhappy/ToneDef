package edu.columbia.cs.jmg.ToneDef;

import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;



public class ToneStream
{
	final int N_BUFFERS = 2;
	final int BUFSIZE = 1024;
	Thread loop, shaper, sender;
	boolean started;
	volatile Integer tone;
	volatile Float increment;
	volatile Float modulatorIncrement;
	
	float[][] buffers = new float[N_BUFFERS][BUFSIZE];
	ConcurrentLinkedQueue<float[]> shapeq, sendq;
	
	boolean stopflag;
	
	
	public ToneStream()
	{
		started = false;
		tone = 0;
		increment = 0.0f;
		modulatorIncrement = 0.0f;
		shapeq = new ConcurrentLinkedQueue<float[]>();
		sendq = new ConcurrentLinkedQueue<float[]>();
		
	}
	
	public void start()
	{
		tone = 0;
		
		//move all buffers to the shaper
		sendq.clear();
		for(int i = 0; i < buffers.length; i++)
			shapeq.add(buffers[i]);
		
		started = true;
		
		shaper = new Thread(new Runnable()
		{
			public void run()
			{
				float[] activeBuffer = null;
				float angle = 0, modulatorAngle = 0, wavtmp, modtmp;
				
				while(true)
				{
					if(!started)
					{
						break;
					}
					while((activeBuffer = shapeq.poll()) == null)
					{
						if(!started)
						{
							return;
						}
						try 
						{
							//shapeq.wait();
							Thread.sleep(5);
						}
						catch (InterruptedException e) 
						{
							return;
						}
					}
					//Log.d("ToneDef","Shaper polled. "+shapeq.size());
					for (int i = 0; i < activeBuffer.length; i++) 
					{
						wavtmp = (float) Math.sin(angle);
						angle += increment;
						modtmp = (float) Math.sin(modulatorAngle) * 0.5f;
						modulatorAngle += modulatorIncrement;
						activeBuffer[i] = wavtmp * modtmp;
					}
					if(angle > 10)
						angle = angle - (int)(angle/(2*Math.PI)) * (float)(2*Math.PI);
					if(modulatorAngle > 10)
						modulatorAngle = modulatorAngle - (int)(modulatorAngle/(2*Math.PI)) * (float)(2*Math.PI);
					sendq.add(activeBuffer);
					//Log.d("ToneDef","shaper added to sender. "+sendq.size());
					
					//sendq.notify();
					activeBuffer = null;
					
				}
			}
		});
		sender = new Thread(new Runnable()
		{
			public void run()
			{
				float[] activeBuffer = null;
				AndroidAudioDevice device = new AndroidAudioDevice();
				
				while(true)
				{
					if(!started)
					{
						device.stop();
						break;
					}
					//Log.d("ToneDef","Sender init polling. "+sendq.size());
					while((activeBuffer = sendq.poll()) == null)
					{
						//Log.d("ToneDef","Sender polling. "+sendq.size());
						if(!started)
						{
							device.stop();
							return;
						}
						try 
						{
							Thread.sleep(1);
						}
						catch (InterruptedException e) 
						{
							return;
						}
						
					}
					//broke out of the loop
					//Log.d("ToneDef","Sender polled. "+sendq.size());
					device.writeSamples(activeBuffer);
					shapeq.add(activeBuffer);
					//Log.d("ToneDef","sender added to shaper. ");
					//shapeq.notify();
					activeBuffer = null;
				}
				
			}
		});
		sender.start();
		shaper.start();
	}
	
	public void stop()
	{
		tone = -1;
		started = false;
		try {
			loop.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void add(int freq)
	{
		//toneq.add(freq);
		tone = freq;
		increment = getIncrement(freq);
	}
	
	public void addModulator(int freq)
	{
		//toneq.add(freq);
		//tone = freq;
		modulatorIncrement = getIncrement(freq);
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
