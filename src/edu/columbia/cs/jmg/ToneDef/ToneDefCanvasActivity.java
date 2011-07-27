package edu.columbia.cs.jmg.ToneDef;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ToneDefCanvasActivity extends Activity {
    private ToneDefService mBoundService;
    boolean mIsBound;
    Button pwrBtn;
    TextView status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	doBindService();
	status = ((TextView) findViewById(R.id.statusTV));
	pwrBtn = (Button) findViewById(R.id.startBtn);
	setStatus("");
	final Object data = getLastNonConfigurationInstance();
	if (data != null) {
	    setStatus(data.toString());
	}

	pwrBtn.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (!mBoundService.getIsStarted()) {
		    mBoundService.start();
		}
		else {
		    mBoundService.stop();
		}
		pwrBtn.setText(mBoundService.getIsStarted() ? R.string.stopBtn
			: R.string.startBtn);
	    }
	});

	findViewById(R.id.tapAreaView).setOnTouchListener(
		new View.OnTouchListener() {

		    public boolean onTouch(View v, MotionEvent me) {
			final double numQuants = 13.0;
			final double numQuantsX = 20.0;
			final double quantSize = v.getHeight() / numQuants;
			final double quantSizeX = v.getWidth() / numQuantsX;
			int quant = (int) (me.getY() / quantSize);
			int quantX = (int) (me.getX() / quantSizeX);
			int freq = indexToFreq(440.0, quant);
			int freqM = quantX - 10 < 0 ? 0 : quantX - 10;
			setStatus("Tone: " + freq + " Hz  Modulator: " + freqM
				+ " Hz");
			mBoundService.addFreq(freq);
			mBoundService.addModulation(freqM);
			return true; // more events from this stroke are wanted
		    }
		});

    }

    private static int indexToFreq(double base, int index) {
	return (int) (base * Math.pow(2, (index) / (12.0)));
    }

    private void setStatus(String statusText) {
	status.setText(statusText);
	if(mBoundService != null)
	    mBoundService.setStatus(statusText);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
	public void onServiceConnected(ComponentName className, IBinder service) {
	    // This is called when the connection with the service has been
	    // established, giving us the service object we can use to
	    // interact with the service. Because we have bound to a explicit
	    // service that we know is running in our own process, we can
	    // cast its IBinder to a concrete class and directly access it.
	    mBoundService = ((ToneDefService.LocalBinder) service).getService();

	    // Tell the user about this for our demo.
	    // Toast.makeText(ToneDefCanvasActivity.this,
	    // R.string.local_service_connected, Toast.LENGTH_SHORT)
	    // .show();
	    pwrBtn.setText(mBoundService.getIsStarted() ? R.string.stopBtn
		    : R.string.startBtn);
	    setStatus(mBoundService.getStatus());
	}

	public void onServiceDisconnected(ComponentName className) {
	    // This is called when the connection with the service has been
	    // unexpectedly disconnected -- that is, its process crashed.
	    // Because it is running in our same process, we should never
	    // see this happen.
	    mBoundService = null;
	    Toast.makeText(ToneDefCanvasActivity.this,
		    R.string.local_service_disconnected, Toast.LENGTH_SHORT)
		    .show();
	}
    };

    void doBindService() {
	// Establish a connection with the service. We use an explicit
	// class name because we want a specific service implementation that
	// we know will be running in our own process (and thus won't be
	// supporting component replacement by other applications).
	bindService(
		new Intent(ToneDefCanvasActivity.this, ToneDefService.class),
		mConnection, Context.BIND_AUTO_CREATE);
	mIsBound = true;
    }

    void doUnbindService() {

	if (mIsBound) {
	    // Detach our existing connection.
	    unbindService(mConnection);
	    mIsBound = false;
	}
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	mIsBound = false; // dont unbind
	return status.getText();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	doUnbindService();
    }
}
