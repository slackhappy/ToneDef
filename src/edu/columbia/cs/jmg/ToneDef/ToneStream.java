package edu.columbia.cs.jmg.ToneDef;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class ToneStream {
    final int N_BUFFERS = 2;
    final int BUFSIZE = 1024;
    Thread shaper, sender;
    boolean started;
    volatile Integer tone;
    volatile Float increment;
    volatile Float modulatorIncrement;

    short[][] buffers = new short[N_BUFFERS][BUFSIZE];
    ConcurrentLinkedQueue<short[]> shapeq, sendq;


    boolean stopflag;
    float angle = 0, modulatorAngle = 0, wavtmp, modtmp;

    public ToneStream() {
	started = false;

	increment = 0.0f;
	modulatorIncrement = 0.0f;

    }

    public void start() {

	if (started != false)
	    return;

	// move all buffers to the shaper
	// NOTE: can't reuse old queues
	shapeq = new ConcurrentLinkedQueue<short[]>();
	sendq = new ConcurrentLinkedQueue<short[]>();
	for (int i = 0; i < buffers.length; i++)
	    shapeq.add(buffers[i]);

	started = true;
	
	//Basically, the producer thread
	shaper = new Thread(new Runnable() {
	    public void run() {
		short[] activeBuffer = null;

		while (true) {
		    if (!started) {
			break;
		    }
		    while ((activeBuffer = shapeq.poll()) == null) {
			// Log.d("ToneDef","Shaper polling. "+sendq.size());
			if (!started) {
			    return;
			}
			try {
			    // shapeq.wait();
			    Thread.sleep(5);
			}
			catch (InterruptedException e) {
			    return;
			}
		    }
		    // Log.d("ToneDef","Shaper polled. "+shapeq.size());
		    for (int i = 0; i < activeBuffer.length; i++) {
			wavtmp = (float) Math.sin(angle);
			angle += increment;
			modtmp = (float) Math.sin(modulatorAngle) * 0.5f;
			modulatorAngle += modulatorIncrement;
			activeBuffer[i] = (short) ((wavtmp * modtmp) * Short.MAX_VALUE);
		    }

		    sendq.add(activeBuffer);
		    // Log.d("ToneDef","shaper added to sender. "+sendq.size());
		    if (angle > 10)
			angle = angle - (int) (angle / (2 * Math.PI))
				* (float) (2 * Math.PI);
		    if (modulatorAngle > 10)
			modulatorAngle = modulatorAngle
				- (int) (modulatorAngle / (2 * Math.PI))
				* (float) (2 * Math.PI);

		    // sendq.notify();
		    activeBuffer = null;

		}
	    }
	});
	
	//The consumer thread
	sender = new Thread(new Runnable() {
	    public void run() {
		short[] activeBuffer = null;
		boolean trackStart = false;
		int minSize = AudioTrack.getMinBufferSize(44100,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT);
		Log.d("ToneDef", "starting minsize: " + minSize);
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
			44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT, minSize,
			AudioTrack.MODE_STREAM);

		while (true) {
		    if (!started) {
			// device.stop();
			break;
		    }
		    // Log.d("ToneDef","Sender init polling. "+sendq.size());
		    while ((activeBuffer = sendq.poll()) == null) {
			Log.d("ToneDef", "Sender polling. " + sendq.size());
			if (!started) {
			    track.stop();
			    track.flush();
			    track.release();
			    return;
			}
			try {
			    Thread.sleep(1);
			}
			catch (InterruptedException e) {
			    return;
			}

		    }
		    // broke out of the loop
		    // Log.d("ToneDef","Sender polled. "+sendq.size());
		    // device.writeSamples(activeBuffer);
		    if (!trackStart) {
			trackStart = true;
			track.play();
		    }
		    track.write(activeBuffer, 0, activeBuffer.length);
		    shapeq.add(activeBuffer);
		    // Log.d("ToneDef","sender added to shaper. ");
		    activeBuffer = null;
		}
		track.stop();
		track.flush();
		track.release();
	    }
	});
	sender.start();
	shaper.start();
    }

    public void stop() {
	started = false;
	try {
	    sender.join();
	    shaper.join();
	}
	catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void add(int freq) {
	// toneq.add(freq);
	tone = freq;
	increment = getIncrement(freq);
    }

    public void addModulator(int freq) {
	// toneq.add(freq);
	// tone = freq;
	modulatorIncrement = getIncrement(freq);
    }

    public boolean getIsStarted() {
	return started;
    }

    private static float getIncrement(float frequency) {
	return (float) (2 * Math.PI) * frequency / 44100; // angular increment
    }

}
