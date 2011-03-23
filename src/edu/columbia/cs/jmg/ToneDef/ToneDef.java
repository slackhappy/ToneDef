package edu.columbia.cs.jmg.ToneDef;

import android.app.Activity;
import android.os.Bundle;
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
				double numQuants = 13.0;
				double quantSize = v.getHeight()/numQuants;
				int quant = (int)(me.getY()/quantSize);
				
				int freq = (int)(440.0 * Math.pow(2, (quant)/(numQuants)));
				setStatus("Freq: "+freq+" note: "+quant);
				stream.add(freq);
				return true; //more events from this stroke are wanted
			}
        });
        
		
        
    }
    
    private void setStatus(String statusText)
    {
    	status.setText(statusText);
    }
}