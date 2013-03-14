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

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppPicker extends Activity{

	PackageManager packMan;
	static List<ApplicationInfo> appInfos;
	static Drawable[] icons;
	static String[] appNames, appPackages;
	ProgressDialog plsWait;
	GetAppList stuff;
	boolean ready;
	int filter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_picker);
		filter = getIntent().getIntExtra("filter", -1);
		plsWait = new ProgressDialog(this);
		plsWait.setCancelable(false);
		stuff = new GetAppList();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		if( appInfos != null ){
			if( appNames[appInfos.size()-1] != null ){
				ready = true;
				return;
			}
		}
		stuff.execute();
	}

	@Override
	protected void onResume(){
		super.onResume();
		if( !ready ) return;
		ArrayAdapter<String> adapter = new AppPickerAdapter(this, appNames);
		ListView appPickerList = (ListView) findViewById(R.id.app_picker_list);
		appPickerList.setAdapter(adapter);
		appPickerList.setOnItemClickListener(
			new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					String app = appPackages[position];
					Prefs prefs = new Prefs(view.getContext());
					for( int i = 0 ; i < prefs.getNumberOfFilters() ; i++){
						if( app.equals(prefs.getFilterApp(i)) && i != filter ){
							finish();
							Toast.makeText(view.getContext(), R.string.app_picker_duplicate, Toast.LENGTH_SHORT).show();
							return;
						}
					}
					setResult(Activity.RESULT_OK, new Intent().setAction(app));
					finish();
				}
			}
		);
		plsWait.dismiss();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		stuff.cancel(true);
	}
	
	private class AppPickerAdapter extends ArrayAdapter<String>{
		private final Context context;
		private final String[] appNames;
		
		public AppPickerAdapter(Context arg0, String[] arg1){
			super(arg0, R.layout.list_item, arg1);
			this.context = arg0;
			this.appNames = arg1;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View itemView = ( (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.list_item, parent, false);
			TextView textView = (TextView) itemView.findViewById(R.id.filter_item_name);
			textView.setText(appNames[position]);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.filter_item_image);
			imageView.setImageDrawable(icons[position]);
			return itemView;
		}
	}

	private class GetAppList extends AsyncTask<Void, Integer, Void>{

		String[] packageBuffer;
		Drawable[] iconBuffer;
		
		@Override
		protected void onPreExecute(){
			packMan = getPackageManager();
			appInfos = packMan.getInstalledApplications(0);
			publishProgress(0);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			appNames = new String[appInfos.size()];
			appPackages = new String[appInfos.size()];
			packageBuffer = new String[appInfos.size()];
			icons = new Drawable[appInfos.size()];
			iconBuffer = new Drawable[appInfos.size()];
			
			for( int i = 0 ; i < appInfos.size() && !isCancelled(); i++){
				appNames[i] = packMan.getApplicationLabel(appInfos.get(i)).toString()+" "+String.valueOf(i);
				packageBuffer[i] = appInfos.get(i).packageName;
				iconBuffer[i] = packMan.getApplicationIcon(appInfos.get(i));
				publishProgress(i+1);
			}
			Arrays.sort(appNames);
			int j;
			for( int i = 0 ; i < appInfos.size() ; i++ ){
				j = Integer.parseInt(appNames[i].substring(appNames[i].lastIndexOf(" ")+1));
				appPackages[i] = packageBuffer[j];
				icons[i] = iconBuffer[j];
				appNames[i] = appNames[i].substring(0, appNames[i].lastIndexOf(" "));
				publishProgress(i+1);
			}
			if( isCancelled() )
				return null;
			publishProgress(-1);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress){
			if( progress[0] == 0 ){
				plsWait.setMax(appInfos.size());
				plsWait.setMessage(getText(R.string.app_picker_retrieve));
				plsWait.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				plsWait.show();
			}else if( progress[0] == -1 ){
				ready = true;
				onResume();
			}
			else{
				plsWait.setProgress(progress[0]);
			}
		}
		
	}
}
