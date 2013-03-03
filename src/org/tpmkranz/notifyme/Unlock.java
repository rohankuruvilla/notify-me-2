/*	Notify Me!, an app to enhance Android(TM)'s abilities to show notifications.
	Copyright (C) 2013 Tom Kranz
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
	Android is a trademark of Google Inc.
*/
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
