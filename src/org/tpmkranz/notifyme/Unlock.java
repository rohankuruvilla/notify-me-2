package org.tpmkranz.notifyme;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager.LayoutParams;

public class Unlock extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		new WaitForUnlock().execute();
	}

	private class WaitForUnlock extends AsyncTask<Void,Void,Boolean>{
		@Override
		protected Boolean doInBackground(Void... params) {
			while( ((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode() ){
				try{
					wait(100);
				}catch(Exception e){
					
				}
				if( !((PowerManager)getSystemService(POWER_SERVICE)).isScreenOn() )
					return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			finish();
			if( result.booleanValue() ){
				try{
					((Notification) ((TemporaryStorage)getApplicationContext()).getParcelable()).contentIntent.send();
				}catch(Exception e){
					
				}
			}
		}
	}
}
