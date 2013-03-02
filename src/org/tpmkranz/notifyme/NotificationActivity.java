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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RemoteViews;

@SuppressLint("NewApi")
public class NotificationActivity extends Activity {

	Prefs prefs;
	boolean big = false;
	boolean finishable = true;
	boolean changeView = false;
	Notification notif;
	AlertDialog dialog;
	int filter;
	View localViews = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(LayoutParams.FLAG_TURN_SCREEN_ON, LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().setFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED, LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		prefs = new Prefs(this);
		filter = ((TemporaryStorage)getApplicationContext()).getFilter();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		big = false;
		notif = (Notification) ((TemporaryStorage)getApplicationContext()).getParcelable();
		showPopup();
	}
	
	private void showPopup() {
		RemoteViews remoteViews;
		boolean showPopup = prefs.isPopupAllowed(filter);
		try{
			if( big )
				remoteViews = notif.bigContentView;
			else
				remoteViews = notif.contentView;		
			localViews = ( (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE) ).inflate(remoteViews.getLayoutId(), null);
			remoteViews.reapply(this, localViews);
		}catch(Exception e){
			showPopup = false;
		}
		if( showPopup ){
			try{
				((ImageView)localViews.findViewById(16908294)).getDrawable().hashCode();
			}catch(Exception e){
				((ImageView)localViews.findViewById(16908294)).setImageDrawable(getOriginalIcon(prefs.getFilterApp(filter)));
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setInverseBackgroundForced(prefs.isBackgroundColorInverted()).setView(localViews).setPositiveButton(R.string.notification_view_button,
					new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try{
							dialog.dismiss();
							startActivity(new Intent(getApplicationContext(), Unlock.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}catch(Exception e){
							
						}
					}
				}
			).setNegativeButton(R.string.notification_dismiss_button, null);
			if( android.os.Build.VERSION.SDK_INT >= 16 && prefs.isExpansionAllowed(filter) ){
				try{
					notif.bigContentView.hashCode();
					builder.setNeutralButton( ( big ? R.string.notification_collapse_button : R.string.notification_expand_button ),
							new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finishable = false;
								changeView = true;
							}
						}
					);
				}catch(Exception e){
					
				}
			}
			dialog = builder.create();
		}else
			dialog = new AlertDialog.Builder(this).setMessage("").create();
		dialog.setOnDismissListener(
			new OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialog) {
					if( finishable )
						finish();
					else{
						finishable = true;
						if( changeView ){
							changeView = false;
							big = !big;
							showPopup();
						}
					}
				}
			}
		);
		dialog.show();
		if( !showPopup )
			dialog.dismiss();
	}

	Drawable getOriginalIcon(String packageName){
		PackageManager packMan = getPackageManager();
		try {
			return packMan.getApplicationIcon(packageName);
		} catch (NameNotFoundException e) {
			return null;
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		finishable = false;
		dialog.dismiss();
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		if( !intent.equals(getIntent()) ){
			finish();
			startActivity(intent);
		}else{
			super.onNewIntent(intent);
		}
	}
	
}
