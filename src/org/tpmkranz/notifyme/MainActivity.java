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
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	Prefs prefs;
	CheckAccessibilityTask stuff;
	int version = 0;
	boolean access;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try{
			if(getIntent().getAction().equals("redraw")){
				finish();
				return;
			}
		}catch(Exception e){
			
		}
		setContentView(R.layout.activity_main);
		try{
			version = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		}catch(Exception e){
			
		}
		prefs = new Prefs(this);
		if( prefs.getPrevVersion() == 0 ){
			prefs.setAccessibilityServiceRunning(false);
			prefs.setPrevVersion(version);
		}else if( prefs.getPrevVersion() < version ){
			prefs.setAccessibilityServiceRunning(false);
			prefs.setPrevVersion(version);
		}else if( prefs.getPrevVersion() > version ){
			prefs.setAccessibilityServiceRunning(false);
			prefs.setPrevVersion(version);
		}
		access = prefs.isAccessibilityServiceRunning();
	}

	@Override
	protected void onResume(){
		super.onResume();
		if( !access ){
			ProgressDialog pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.main_check_checking));
			pDialog.setMax(3000);
			pDialog.setCancelable(false);
			pDialog.show();
			stuff = new CheckAccessibilityTask();
			stuff.execute(pDialog);
			return;
		}
		ListView mainFilterList = (ListView) this.findViewById(R.id.main_filter_list);
		String[] filterApps = new String[prefs.getNumberOfFilters()+1];
		for( int i = 0 ; i < filterApps.length ; i++ ){
			if( i == prefs.getNumberOfFilters() ){
				filterApps[i] = "JOKER";
			}else{
				filterApps[i] = prefs.getFilterApp(i);
			}
		}
		ArrayAdapter<String> adapter = new MainFilterAdapter(this, filterApps);
		mainFilterList.setAdapter(adapter);
		mainFilterList.setOnItemClickListener(
			new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					Intent editFilterIntent = new Intent(parent.getContext(), EditFilterActivity.class);
					if( position == prefs.getNumberOfFilters() ){
						editFilterIntent.setAction("new");
					}else{
						editFilterIntent.setAction("edit");
						editFilterIntent.putExtra("filter", position);
					}
					startActivity(editFilterIntent);
				}
			}
		);
		mainFilterList.setOnItemLongClickListener(
			new OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
					final int filter = position;
					if( filter == prefs.getNumberOfFilters() )
						return true;
					final View finalView = view;
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					try{
					builder.setTitle(getResources().getString(R.string.main_remove_title1)+" "+
							((TextView)((RelativeLayout)view).getChildAt(1)).getText()
							+" "+getResources().getString(R.string.main_remove_title2));
					builder.setIcon(((ImageView)((RelativeLayout)view).getChildAt(0)).getDrawable());
					}catch(Exception e){
						
					}
					builder.setPositiveButton(R.string.main_remove_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								prefs.removeFilter(filter);
								startActivity(new Intent(finalView.getContext(), MainActivity.class).setAction("redraw"));
							}
						}
					);
					builder.setNegativeButton(R.string.main_remove_cancel, null);
					builder.show();
					return true;
				}
			}
		);
	}
	
	public class MainFilterAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
		private final PackageManager packMan;
		
		public MainFilterAdapter(Context arg0, String[] arg1){
			super(arg0, R.layout.list_item, arg1);
			this.context = arg0;
			this.values = arg1;
			this.packMan = context.getPackageManager();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View itemView = ( (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.list_item, parent, false);
			TextView textView = (TextView) itemView.findViewById(R.id.filter_item_name);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.filter_item_image);
			if( values[position].equals("JOKER") ){
				textView.setText(R.string.main_add_to_list);
				imageView.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_add));
				return itemView;
			}
			ApplicationInfo appInfo;
			try {
				appInfo = packMan.getApplicationInfo( values[position], 0 );
				textView.setText(packMan.getApplicationLabel(appInfo));
				imageView.setImageDrawable(packMan.getApplicationIcon(appInfo));
			} catch (NameNotFoundException e) {
				
			}
			return itemView;
		}
	}

	private class CheckAccessibilityTask extends AsyncTask<ProgressDialog, Long, Boolean>{

		ProgressDialog pDialog;
		Notification notif;
		
		@Override
		protected Boolean doInBackground(ProgressDialog... args) {
			pDialog = args[0];
			notif = new NotificationCompat.Builder(getApplicationContext()).setTicker("Actioni contrariam semper et aequalem esse reactionem.")
					.setSmallIcon(R.drawable.ic_launcher).setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT)).build();
			this.publishProgress(-1L);
			long t = System.currentTimeMillis() ;
			while( System.currentTimeMillis() - t < 3500L ){
				if( prefs.isAccessibilityServiceRunning() ){
					return true;
				}else{
					publishProgress(System.currentTimeMillis()-t);
				}
			}
			return false;
		}
		
		@Override
		protected void onProgressUpdate(Long... progress){
			if(progress[0].longValue() == -1L)
				((NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE)).notify(21, notif);
			else
				pDialog.setProgress( progress[0].intValue() );
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			pDialog.dismiss();
			AlertDialog aDialog;
			access = result.booleanValue();
			((NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE)).cancel(21);
			if( access )
				aDialog = new AlertDialog.Builder(pDialog.getContext()).setMessage(R.string.main_check_good_message).setPositiveButton(R.string.main_check_good_button, null).create();
			else
				aDialog = new AlertDialog.Builder(pDialog.getContext()).setMessage(getResources().getString(R.string.main_check_bad_message)
						+(version == prefs.getPrevVersion() ? getResources().getString(R.string.main_check_bad_message0) : ""))
				.setPositiveButton(R.string.main_check_bad_button, null).setCancelable(false).create();
			aDialog.setOnDismissListener(
				new DialogInterface.OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface dialog) {
						prefs.setAccessibilityServiceRunning(access);
						if( access ){
							finish();
							startActivity(getIntent());
						}else{
							startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
						}
					}
				}
			);
			aDialog.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId()){
			case R.id.main_menu_checkaccessibility:
				prefs.setAccessibilityServiceRunning(false);
				finish();
				startActivity(getIntent());
				return true;
			case R.id.main_menu_popup:
				final View view = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main_menu_popup, null);
				((CheckBox)view.findViewById(R.id.main_menu_popup_background_checkbox)).setChecked(prefs.isBackgroundColorInverted());
				view.findViewById(R.id.main_menu_popup_background_caption).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							((CheckBox)view.findViewById(R.id.main_menu_popup_background_checkbox)).toggle();
						}
					}
				);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).setMax(255);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).setOnSeekBarChangeListener(
					new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if( fromUser ){
								((EditText)view.findViewById(R.id.main_menu_popup_color_edit_r)).setText(( progress == 0 ? "" : String.valueOf(progress) ));
							}

							((ImageView)view.findViewById(R.id.main_menu_popup_color_preview)).setImageDrawable(new ColorDrawable(Color.rgb(progress, ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).getProgress(), ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).getProgress())));
						}
					}
				);
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_r)).addTextChangedListener(
					new TextWatcher(){
						@Override
						public void afterTextChanged(Editable s) {
							try{
								if( Integer.parseInt(s.toString()) > 255 ){
									s.replace(0, s.length(), "255");
									return;
								}else if( Integer.parseInt(s.toString()) < 0 ){
									s.replace(0, s.length(), "0");
									return;
								}
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).setProgress(Integer.parseInt(s.toString()));
							}catch(Exception e){
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).setProgress(0);
								s.clear();
							}
						}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
					}
				);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).setMax(255);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).setOnSeekBarChangeListener(
					new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if( fromUser ){
								((EditText)view.findViewById(R.id.main_menu_popup_color_edit_g)).setText(( progress == 0 ? "" : String.valueOf(progress) ));
							}
							((ImageView)view.findViewById(R.id.main_menu_popup_color_preview)).setImageDrawable(new ColorDrawable(Color.rgb(((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).getProgress(), progress, ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).getProgress())));
						}
					}
				);
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_g)).addTextChangedListener(
					new TextWatcher(){
						@Override
						public void afterTextChanged(Editable s) {
							try{
								if( Integer.parseInt(s.toString()) > 255 ){
									s.replace(0, s.length(), "255");
									return;
								}else if( Integer.parseInt(s.toString()) < 0 ){
									s.replace(0, s.length(), "0");
									return;
								}
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).setProgress(Integer.parseInt(s.toString()));
							}catch(Exception e){
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).setProgress(0);
								s.clear();
							}
						}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
					}
				);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).setMax(255);
				((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).setOnSeekBarChangeListener(
					new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if( fromUser ){
								((EditText)view.findViewById(R.id.main_menu_popup_color_edit_b)).setText(( progress == 0 ? "" : String.valueOf(progress) ));
							}
							((ImageView)view.findViewById(R.id.main_menu_popup_color_preview)).setImageDrawable(new ColorDrawable(Color.rgb(((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).getProgress(), ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).getProgress(), progress)));
						}
					}
				);
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_b)).addTextChangedListener(
					new TextWatcher(){
						@Override
						public void afterTextChanged(Editable s) {
							try{
								if( Integer.parseInt(s.toString()) > 255 ){
									s.replace(0, s.length(), "255");
									return;
								}else if( Integer.parseInt(s.toString()) < 0 ){
									s.replace(0, s.length(), "0");
									return;
								}
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).setProgress(Integer.parseInt(s.toString()));
							}catch(Exception e){
								((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).setProgress(0);
								s.clear();
							}
						}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
					}
				);
				((ImageView)view.findViewById(R.id.main_menu_popup_color_preview)).setImageDrawable(new ColorDrawable(Color.rgb(prefs.getSliderBackgroundR(), prefs.getSliderBackgroundG(), prefs.getSliderBackgroundB())));
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_r)).setText(String.valueOf(prefs.getSliderBackgroundR()));
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_g)).setText(String.valueOf(prefs.getSliderBackgroundG()));
				((EditText)view.findViewById(R.id.main_menu_popup_color_edit_b)).setText(String.valueOf(prefs.getSliderBackgroundB()));
				((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).setChecked(prefs.isInterfaceSlider());
				((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(0).setEnabled(((CheckBox)v).isChecked());
							for( int i = 0 ; i < 3 ; i++){
								((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(0).setEnabled(((CheckBox)v).isChecked());
								((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(1).setEnabled(((CheckBox)v).isChecked());
							}
							((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(2).setVisibility(( ((CheckBox)v).isChecked() ? View.VISIBLE : View.INVISIBLE ));
						}
					}
				);
				((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(0).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
				for( int i = 0 ; i < 3 ; i++){
					((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(0).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
					((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(1).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
				}
				((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(2).setVisibility(( ((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked() ? View.VISIBLE : View.INVISIBLE ));
				view.findViewById(R.id.main_menu_popup_interface_caption).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).toggle();
							((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(0).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
							for( int i = 0 ; i < 3 ; i++){
								((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(0).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
								((ViewGroup)((ViewGroup)((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(1)).getChildAt(i)).getChildAt(1).setEnabled(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
							}
							((ViewGroup)view.findViewById(R.id.main_menu_popup_color)).getChildAt(2).setVisibility(( ((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked() ? View.VISIBLE : View.INVISIBLE ));
						}
					}
				);
				new AlertDialog.Builder(this).setView(view).setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							prefs.setBackgroundColorInverted(((CheckBox)view.findViewById(R.id.main_menu_popup_background_checkbox)).isChecked());
							prefs.setInterfaceSlider(((CheckBox)view.findViewById(R.id.main_menu_popup_interface_checkbox)).isChecked());
							prefs.setSliderBackground(((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_r)).getProgress(), ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_g)).getProgress(), ((SeekBar)view.findViewById(R.id.main_menu_popup_color_slider_b)).getProgress());
						}
					}
				).setNegativeButton("Cancel", null).show();
				return true;
			case R.id.main_menu_help:
				new AlertDialog.Builder(this).setMessage(R.string.main_menu_help_message).setTitle(R.string.main_menu_help_title)
				.setPositiveButton(R.string.main_menu_help_ok_button, null).setNegativeButton(R.string.main_menu_help_question_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/notify-me/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					}
				).show();
				return true;
			case R.id.main_menu_about:
				new AlertDialog.Builder(this).setMessage(R.string.main_menu_about_message).setTitle(R.string.main_menu_about_title)
				.setPositiveButton(R.string.main_menu_about_ok_button, null).setNegativeButton(R.string.main_menu_about_donate_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WB8TZ36M36L3Q")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					}
				).setNeutralButton(R.string.main_menu_about_license_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/licenses/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					}
				).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
}
