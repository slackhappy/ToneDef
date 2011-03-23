package edu.columbia.cs.jmg.ToneDef;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;


public class ToneDef extends Activity {
	ToneStream stream;
	boolean started;
	TextView status;
	Button pwrBtn;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        stream = new ToneStream();
        status = ((TextView)findViewById(R.id.statusTV));
        setStatus("");
        
        
        (pwrBtn = (Button)findViewById(R.id.startBtn)).setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v)
        	{
        		if(!stream.isStarted())
        		{
        			stream.start();
        			setStatus("Started");
        			pwrBtn.setText(R.string.stopBtn);
        		}
        		else
        		{
        			
        			stream.stop();
        			setStatus("Stopped");
        			pwrBtn.setText(R.string.startBtn);
        		}
        		
        	}
        });
        
        
        
        findViewById(R.id.tapAreaView).setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent me) {
				final double numQuants = 13.0;
				final double quantSize = v.getHeight()/numQuants;
				final double quantSizeX = v.getWidth()/numQuants;
				int quant = (int)(me.getY()/quantSize);
				int quantX = (int)(me.getX()/quantSizeX);
				int freq = indexToFreq(440.0, quant);
				int freqM = 5 + quantX * 50;
				setStatus("Freq: "+freq+" note: "+quant+" Modulator: "+freqM);
				stream.add(freq);
				stream.addModulator(freqM);
				return true; //more events from this stroke are wanted
			}
        });
        
        new Thread(new Runnable()
        {
        	public void run()
        	{
        		long statsCounter = 0;
				long statsNumerator = 0; 
        		int[] indices = {0, 4, 7, 11, 7, 4, 4, 7, 11, 14, 11, 7, 0, 4, 7, 11, 7, 4, 5, 9, 12, 16, 9, 5, 0, 4, 7, 11, 7, 4, 2, 5, 9, 12, 9, 5, 5, 9, 12, 16, 9, 5, 0, 4, 7, 11, 7, 4, 0};
        		while(true)
        		for(int i = 0; i<indices.length; i++)
        		{
        			
        			long start = System.nanoTime();
        			stream.add(indexToFreq(440.0, indices[i]));
        			try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						break;
					}
					long elapsed = (System.nanoTime() - start)/1000000;
					statsNumerator += elapsed;
					statsCounter++;
					if(statsCounter % 2 == 0)
					{
						Log.d("ToneDef","Loopstats: "+(statsNumerator/statsCounter)+" msec wait.");
					}
					
        		}
        	}
        });//.start();
        
		
        
    }
    
    private void setStatus(String statusText)
    {
    	status.setText(statusText);
    }
    
    private static int indexToFreq(double base, int index)
    {
		return (int)(base * Math.pow(2, (index)/(12.0)));
    }
}