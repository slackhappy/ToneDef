package edu.columbia.cs.jmg.ToneDef;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;


public class ToneDefService extends Service {
    private NotificationManager mNM;
    ToneStream stream;
    boolean isStarted;
    String status;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
	ToneDefService getService() {
	    return ToneDefService.this;
	}
    }
    
    public void setStatus(String status){
	this.status = status;
    }
    
    public String getStatus(){
	return this.status;
    }

    public void start() {
	if(isStarted)
	    return;
	stream.start();
	showNotification();
	isStarted = true;
    }

    public void stop() {
	if(!isStarted)
	    return;
	isStarted = false;
	stream.stop();
	mNM.cancel(NOTIFICATION);
    }

    public boolean getIsStarted() {
	return isStarted;
    }

    public void addFreq(int freq) {
	stream.add(freq);
    }

    public void addModulation(int modulationFreq) {
	stream.addModulator(modulationFreq);
    }

    @Override
    public void onCreate() {
	mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	stream = new ToneStream();
	isStarted = false;
	status = "";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i("ToneDefService", "Received start id " + startId + ": " + intent);
	// We want this service to continue running until it is explicitly
	// stopped, so return sticky.
	return START_STICKY;
    }

    @Override
    public void onDestroy() {
	stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
	// In this sample, we'll use the same text for the ticker and the
	// expanded notification
	CharSequence text = getText(R.string.local_service_started);

	// Set the icon, scrolling text and timestamp
	Notification notification = new Notification(
		android.R.drawable.ic_media_play, text, System
			.currentTimeMillis());

	// The PendingIntent to launch our activity if the user selects this
	// notification
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		new Intent(this, ToneDefCanvasActivity.class), 0);

	// Set the info for the views that show in the notification panel.
	notification.setLatestEventInfo(this,
		getText(R.string.local_service_label), text, contentIntent);

	// Send the notification.
	mNM.notify(NOTIFICATION, notification);
    }
}
