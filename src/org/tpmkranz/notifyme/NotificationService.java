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

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.Intent;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.TextView;

public class NotificationService extends AccessibilityService {

	Prefs prefs;
	int filter;
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if( event.getPackageName().equals("org.tpmkranz.notifyme") ){
			if( event.getText().get(0).equals("Actioni contrariam semper et aequalem esse reactionem.") ){
				prefs.setAccessibilityServiceRunning(true);
			}
		}
		if( !event.getClassName().equals("android.app.Notification") || ( ((PowerManager)getSystemService(POWER_SERVICE)).isScreenOn() && !((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode() ) )
			return;
		if(filterMatch(event)) triggerNotification(event);
	}

	@SuppressLint("NewApi")
	private boolean filterMatch(AccessibilityEvent event) {

		boolean filterMatch = false;
		for( int i = 0; i < prefs.getNumberOfFilters() && !filterMatch; i++ ){
			if( event.getPackageName().equals(prefs.getFilterApp(i)) ){
				filter = i;
				if( prefs.hasFilterKeywords(i) ){
					String notificationContents = ( event.getText().size() == 0 ? "" : event.getText().get(0).toString() );
					try{
						Notification notification = (Notification) event.getParcelableData();
						RemoteViews remoteViews = notification.contentView;
						LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
						ViewGroup localViews = (ViewGroup) inflater.inflate(remoteViews.getLayoutId(), null);
						remoteViews.reapply(this, localViews);
						String piece = "";
						for( int j = 16900000 ; j < 17000000 ; j++ ){
							try{
								piece = "\n"+( (TextView) localViews.findViewById(j) ).getText();
								notificationContents.concat(piece);
							}catch(Exception e){
								
							}
						}
						if(android.os.Build.VERSION.SDK_INT >= 16){
							try{
								remoteViews = notification.bigContentView;
								localViews = (ViewGroup) inflater.inflate(remoteViews.getLayoutId(), null);
								remoteViews.reapply(this, localViews);
								piece = "";
								for( int j = 16900000 ; j < 17000000 ; j++ ){
									try{
										piece = "\n"+( (TextView) localViews.findViewById(j) ).getText();
										notificationContents.concat(piece);
									}catch(Exception e){
										
									}
								}
							}catch(Exception e){
								
							}
						}
					}catch(Exception e){
						
					}
					String[] keywords = prefs.getFilterKeywords(i);
					for( int j = 0 ; j < keywords.length && !filterMatch ; j++ ){
						if( notificationContents.contains(keywords[j]) && !keywords.equals("") ){
							filterMatch = true;
						}
					}
				}else{
					filterMatch = true;
				}
			}
		}
		return filterMatch;
	}

	private void triggerNotification(AccessibilityEvent event) {
		((TemporaryStorage)getApplicationContext()).storeStuff(event.getParcelableData());
		((TemporaryStorage)getApplicationContext()).storeStuff(filter);
		startActivity(new Intent(this, NotificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) );
	}

	@Override
	protected void onServiceConnected(){
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		info.flags = AccessibilityServiceInfo.DEFAULT;
		this.setServiceInfo(info);
		prefs = new Prefs(this);
	}
	
	@Override
	public void onInterrupt() {

	}

}
