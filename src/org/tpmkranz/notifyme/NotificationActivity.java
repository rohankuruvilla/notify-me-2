package org.tpmkranz.notifyme;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class NotificationActivity extends Activity {

	Prefs prefs;
	int filter;
	boolean big, showPopup, touchValid, triggers, screenWasOff;
	Notification notif;
	RemoteViews remViews;
	View nView;
	ViewGroup pView;
	SliderSurfaceView sView;
	float X, lastX;
	DrawTask dTask;
	GestureDetector geDet;
	AlertDialog dialog;
	List<Parcelable> notifs;
	List<View> displayedViews;
	LinearLayout llh;
	int scrolledIdx = 0;
	public boolean notifDeleted = false;

	public enum GestureEvent {
		SlideRight, SlideLeft, SlideDown, SlideUp, DoubleTap, SingleTap, LongPress
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		screenWasOff = !((PowerManager) getSystemService(POWER_SERVICE))
				.isScreenOn();
		if (!((KeyguardManager) getSystemService(KEYGUARD_SERVICE))
				.inKeyguardRestrictedInputMode()) {
			setTheme(R.style.Transparent);			
		}
		getWindow().setFlags(LayoutParams.FLAG_TURN_SCREEN_ON,
				LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().setFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED,
				LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		prefs = new Prefs(this);
		dTask = new DrawTask();
		displayedViews = new ArrayList<View>();
		
		if(dialog != null && dialog.isShowing()) {
			Log.i("NotificationsActivityDebug", "on create: dialog exists showing");
			dialog.dismiss();
		} else if(dialog != null) {
			Log.i("NotificationsActivityDebug", "on create: dialog exists not showing");
			dialog.dismiss();
		}
		Log.i("NotificationsActivityDebug", "on create");
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		try {
			screenWasOff = !((PowerManager) getSystemService(POWER_SERVICE))
					.isScreenOn();
			if (!((KeyguardManager) getSystemService(KEYGUARD_SERVICE))
					.inKeyguardRestrictedInputMode()) {
				setTheme(R.style.Transparent);			
			}
			getWindow().setFlags(LayoutParams.FLAG_TURN_SCREEN_ON,
					LayoutParams.FLAG_TURN_SCREEN_ON);
			getWindow().setFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED,
					LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			
			super.onResume();
			notifs = (List<Parcelable>) ((TemporaryStorage) getApplicationContext())
					.getParcelable();
			notif = (Notification) notifs.get(0);

			// if( prefs.expandByDefault(filter) &&
			// android.os.Build.VERSION.SDK_INT >= 16 ){
			big = false;//((TemporaryStorage) getApplicationContext()).getIsBig();
			if (android.os.Build.VERSION.SDK_INT >= 16 && big) {
				try {
					big = true;
				} catch (Exception e) {

				}
			}
			try {
				if (((TemporaryStorage) getApplicationContext()).getTimeout() == 0L
						&& prefs.getScreenTimeout() != 0L && screenWasOff) {
					((TemporaryStorage) getApplicationContext())
							.storeStuff(Settings.System.getLong(
									getContentResolver(),
									Settings.System.SCREEN_OFF_TIMEOUT));
					Settings.System.putLong(getContentResolver(),
							Settings.System.SCREEN_OFF_TIMEOUT,
							prefs.getScreenTimeout());
				}
			} catch (Exception e) {

			}
			Log.i("NotificationsActivityDebug", "resuming");
			if (!preparePopup())
				return;
			if (prefs.isInterfaceSlider())
				showPopupSlider();
			else
				showPopupButton();
		} catch (Exception e) {

		}
	}

	@SuppressLint("NewApi")
	private boolean preparePopup() {
		try {
			if(dialog != null && dialog.isShowing()) {
				Log.i("NotificationsActivityDebug", "dismissing dialog");
				dialog.dismiss();
				dialog = null;
			}

				dialog = new AlertDialog.Builder(this).create();
				dialog.setCanceledOnTouchOutside(false);
				Log.i("NotificationsActivityDebug", "creating dialog "+dialog.toString());

				dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
			return true;
		} catch (Exception e) {

		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private void showPopupSlider() {
		try {
			displayedViews.clear();
			CustomScrollView sv = new CustomScrollView(this);
			sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			llh = new LinearLayout(this);
			llh.setOrientation(LinearLayout.VERTICAL);
			llh.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			sv.addView(llh);
			sv.setBackgroundColor(Color.TRANSPARENT);
			llh.setBackgroundColor(Color.TRANSPARENT);

			Log.i("NotificationsActivityDebug", "notifs size: "+notifs.size()+" displayedviews size: "+displayedViews.size());

			for (int i = 0; i < notifs.size(); ++i) {
				notif = (Notification) notifs.get(i);

				if (big)
					remViews = notif.bigContentView;
				else
					remViews = notif.contentView;

				pView = (ViewGroup) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.slider_popup, null);
				nView = remViews.apply(this, pView);

				pView.setBackgroundColor(Color.TRANSPARENT);

				llh.addView(pView);
				pView.addView(nView, 0, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				displayedViews.add(pView);

			}

			Log.i("NotificationsActivityDebug", "showing dialog process: ");
			UnlockListener sliderListner = new UnlockListener();
			geDet = new GestureDetector(sliderListner);
			llh.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {


                    /*
                    *  Handle revert to original position animation on touchEnded
                    */

                    // Bug: when notification is deleted, it does a glitchy animation for the next notification in the list.
                    // Should have been fixed with notifDeleted flag, but is not.

					if (!notifDeleted && event.getAction() == MotionEvent.ACTION_UP && scrolledIdx >= 0 && scrolledIdx < displayedViews.size()) {
						android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) displayedViews
									.get(scrolledIdx).getLayoutParams();

							displayedViews.get(scrolledIdx).setVisibility(View.VISIBLE);
							TranslateAnimation anim = new TranslateAnimation(
									lp.leftMargin, 0, 0, 0);
							AlphaAnimation anim2 = new AlphaAnimation(
									displayedViews.get(scrolledIdx).getAlpha(), 1.0f);

							anim.setDuration(200);
							anim.setFillAfter(true);
							
							anim2.setDuration(200);
							anim2.setFillAfter(true);
							
							AnimationSet as = new AnimationSet(true);
							as.setFillEnabled(true);
							as.addAnimation(anim);
							as.addAnimation(anim2);
							displayedViews.get(scrolledIdx).startAnimation(as);

							lp.leftMargin = 0;
							lp.rightMargin = 0;
							displayedViews.get(scrolledIdx).setLayoutParams(lp);
							displayedViews.get(scrolledIdx).setAlpha(1);
				
						
					}
					notifDeleted = false;
					return geDet.onTouchEvent(event);
				}
			});
			dialog.setView(sv);
			dTask.cancel(true);
			dTask = new DrawTask();
			dTask.execute();
			Log.i("NotificationsActivityDebug", "showing dialog: "+dialog.toString());
			dialog.show();
/*
 *          The following code would have been ideal if it had worked.
 *          Code to make the background of the notifications
 *          list transparent.
*/

//			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//			lp.copyFrom(dialog.getWindow().getAttributes());
//			lp.width = WindowManager.LayoutParams.FILL_PARENT;
//			lp.height = WindowManager.LayoutParams.FILL_PARENT;
//			dialog.getWindow().setAttributes(lp);
//			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		} catch (Exception e) {

		}
	}

	@SuppressLint("NewApi")
	private void showPopupButton() {

        /*
         * DO NOT KNOW IF THIS WORKS. POPUP BUTTON MODE NEEDS TO BE DISABLED.
         */

		nView = remViews.apply(this, null);
		dialog.setView(nView);
		dialog.setButton(Dialog.BUTTON_POSITIVE,
				this.getText(R.string.notification_view_button),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						startActivity(new Intent(getApplicationContext(),
								Unlock.class)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
				});
		dialog.setButton(Dialog.BUTTON_NEGATIVE,
				this.getText(R.string.notification_dismiss_button),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			try {
				notif.bigContentView.hashCode();
				dialog.setButton(Dialog.BUTTON_NEUTRAL,
						getText(big ? R.string.notification_collapse_button
								: R.string.notification_expand_button),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								big = !big;
								if (preparePopup())
									showPopupButton();
							}
						});
			} catch (Exception e) {

			}
		}
		dialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (prefs.getScreenTimeout() != 0L && screenWasOff)
			Settings.System.putLong(getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT,
					((TemporaryStorage) getApplicationContext()).getTimeout());
		((TemporaryStorage) getApplicationContext()).storeStuff(0L);
		Log.i("NotificationsActivityDebug", "pausing");
		if(dialog.isShowing()) {
			dialog.dismiss();
			Log.i("NotificationsActivityDebug", "pausing and dismissing");
		}
		dTask.cancel(true);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (!getIntent().equals(intent)) {
			finish();
			startActivity(intent);
		} else
			super.onNewIntent(intent);
	}

	private class WaitForLightTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			while (!((PowerManager) getSystemService(POWER_SERVICE))
					.isScreenOn())
				;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			finish();
		}
	}

	private class DrawTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
	}

	private class UnlockListener extends SimpleOnGestureListener {
		float[] a = new float[2];
		float[] b = new float[2];
		float prevx = 0;
		View thisView;
		int thisIdx, leftMargin = 0;
		Map<Integer, Float> lastXs = new HashMap<Integer, Float>();

        public void setView(View v) {
			thisView = v;
		}

		public void setIndex(int idx) {
			thisIdx = idx;
		}

		private boolean contains(View v, float x, float y) {
			int[] location = { 1, 1 };
			v.getLocationOnScreen(location);

			if (x > location[0] && y > location[1]
					&& y <= (location[1] + v.getHeight())
					&& x <= (location[0] + v.getWidth())) {

				return true;
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			try {

                /*
                 * Get the notification being touched (stored in thisView)
                 * Also get the index of the current notification (stored in thisIdx)
                 */

				for (int i = 0; i < displayedViews.size(); ++i) {
					if (contains(displayedViews.get(i), ev.getRawX(),
							ev.getRawY())) {
						thisView = displayedViews.get(i);
						thisIdx = i;
						break;
					}
				}

				a[0] = ev.getX();
				a[1] = ev.getY();

				prevx = ev.getX();
				triggers = true;
				android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) thisView
						.getLayoutParams();
				leftMargin = lp.leftMargin;
				((TemporaryStorage) getApplicationContext())
						.setLongPress(false);
				return true;
			} catch (Exception e) {

			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float dX,
				float dY) {

			triggers = false;
			try {

                /*
                 * Update position of Notification, and adjust opacity.
                */

                if(Math.abs(dY) > Math.abs(dX) - 2) return true;
				android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) thisView
						.getLayoutParams();
				lp.leftMargin = (int) (lp.leftMargin - dX);
				lp.rightMargin = (int) (-(lp.leftMargin - dX));
				thisView.setLayoutParams(lp);
				float newOpacity = 2*Math.abs(ev2.getX() - a[0]) / 720;
				if (ev2.getX() - a[0] > 0) {
					thisView.setAlpha(1 - (newOpacity > 1.0f? 1.0f : newOpacity));
				} else {
					thisView.setAlpha(1 - (newOpacity > 1.0f? 1.0f  : newOpacity) );
				}
				scrolledIdx = thisIdx;
				return true;
			} catch (Exception e) {

			}
			return true;
		}

		private void toggleContentView() {
			try {
				big = !big;
				((TemporaryStorage) getApplicationContext())
				.setIsBig(big);
				if (preparePopup())
					showPopupSlider();
				else
					finish();
			} catch (Exception e) {

			}
		}

		@Override
		public void onLongPress(MotionEvent ev) {
			try {
				((TemporaryStorage) getApplicationContext()).setLongPress(true);
				toggleContentView();
			} catch (Exception e) {

			}
		}

		@SuppressLint("NewApi")
		@Override
		public boolean onFling(MotionEvent ev1, MotionEvent ev2, float vX,
				float vY) {

			try {
				if (android.os.Build.VERSION.SDK_INT < 16 && triggers) {
					return true;
				}

                 if (vX < 0
						&& (Math.abs(vX) > 4 * getResources()
								.getDisplayMetrics().densityDpi
						|| Math.abs(ev2.getX() - a[0]) > thisView.getWidth() / 2)) {  // Slide left


                    /*
                     * Remove from TemporaryStorage.
                     */
					((TemporaryStorage) getApplicationContext())
							.removeParcelable(thisIdx);
					notifs = (List<Parcelable>) ((TemporaryStorage) getApplicationContext()) 
							.getParcelable();
					notifDeleted = true;
					if (notifs.size() < 1) {
						dialog.cancel();
					} else {
						scrolledIdx = -1;

                        /*
                         * Remove from LinearLayout (List displaying notifications)
                         */

						llh.removeViewAt(thisIdx);
						displayedViews.remove(thisIdx);						
					}

				} // End slide left
			    else if (vX > 0
						&& (Math.abs(vX) > 4 * getResources()
								.getDisplayMetrics().densityDpi
						|| Math.abs(ev2.getX() - a[0]) > thisView.getWidth() / 2)) { // Slide right
					((TemporaryStorage) getApplicationContext())
							.setCurrentIndex(thisIdx);
					dialog.cancel();
					startActivity(new Intent(getApplicationContext(),
							Unlock.class));
				} // End slide right
				else if(vY < 0 || vY > 0
                        && (Math.abs(vY) > 4 * getResources()
                        .getDisplayMetrics().densityDpi
                        || Math.abs(ev2.getY() - b[0]) > thisView.getWidth() / 2)) { // Slide up/down
                    ((TemporaryStorage) getApplicationContext()).setLongPress(true);
                    toggleContentView();
                }
				return true;

			} catch (Exception e) {

			}
			return true;
		}	

	}

}
