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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Prefs {
	private final SharedPreferences prefs;
	private final Editor edit;
	
	protected Prefs(Context context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		edit = prefs.edit();
	}
	
	protected int getPrevVersion(){
		return prefs.getInt("PreviousVersion", 0);
	}
	
	protected void setPrevVersion(int newVersion){
		edit.putInt("PreviousVersion", newVersion);
	}
	
	protected boolean isAccessibilityServiceRunning(){
		return prefs.getBoolean("Accessibility", false);
	}
	
	protected void setAccessibilityServiceRunning(boolean running){
		edit.putBoolean("Accessibility", running);
		edit.commit();
	}
	
	protected boolean isBackgroundColorInverted(){
		return prefs.getBoolean("BackgroundInverted", true);
	}
	
	protected void invertBackgroundColor(){
		edit.putBoolean("BackgroundInverted", !isBackgroundColorInverted());
		edit.commit();
	}
	
	protected int getNumberOfFilters(){
		return prefs.getInt("numberOfFilters", 0);
	}
	
	protected String getFilterApp(int filter){
		return prefs.getString("filter"+String.valueOf(filter)+"App", "");
	}
	
	protected boolean hasFilterKeywords(int filter){
		return ( prefs.getInt("filter"+String.valueOf(filter)+"numberOfKeywords", 0) > 0 );
	}
	
	protected boolean isPopupAllowed(int filter){
		return prefs.getBoolean("filter"+String.valueOf(filter)+"Popup", true);
	}
	
	protected boolean isExpansionAllowed(int filter){
		return prefs.getBoolean("filter"+String.valueOf(filter)+"Expansion", true);
	}
	
	protected String[] getFilterKeywords(int filter){
		int numberOfKeywords = prefs.getInt("filter"+String.valueOf(filter)+"numberOfKeywords", 0);
		String[] keywords = new String[numberOfKeywords];
		for(int i = 0 ; i < numberOfKeywords ; i++ ){
			keywords[i] = prefs.getString("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(i), "");
		}
		return keywords;
	}
	
	protected void addFilter(String app, boolean popupAllowed, boolean expansionAllowed){
		int filter = getNumberOfFilters();
		edit.putString("filter"+String.valueOf(filter)+"App", app);
		edit.putBoolean("filter"+String.valueOf(filter)+"Popup", popupAllowed);
		edit.putBoolean("filter"+String.valueOf(filter)+"Expansion", expansionAllowed);
		edit.putInt( "numberOfFilters", filter + 1 );
		edit.commit();
	}

	protected void addFilter(String app, boolean popupAllowed, boolean expansionAllowed, String[] keywords){
		int filter = getNumberOfFilters();
		edit.putString("filter"+String.valueOf(filter)+"App", app);
		edit.putBoolean("filter"+String.valueOf(filter)+"Popup", popupAllowed);
		edit.putBoolean("filter"+String.valueOf(filter)+"Expansion", expansionAllowed);
		int pos = 0;
		for( int i = 0 ; i < keywords.length ; i++ ){
			if( !isUseless(keywords, i) ){
				edit.putString("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(pos++), cleaned(keywords[i]));
			}
		}
		edit.putInt("filter"+String.valueOf(filter)+"numberOfKeywords", pos);
		edit.putInt( "numberOfFilters", filter + 1 );
		edit.commit();
	}
	
	protected void editFilter(int filter, String app, boolean popupAllowed, boolean expansionAllowed){
		for( int i = 0 ; i < prefs.getInt("filter"+String.valueOf(filter)+"numberOfKeywords", 0) ; i++ ){
			edit.remove("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(i));
		}
		edit.remove("filter"+String.valueOf(filter)+"numberOfKeywords");
		edit.putString("filter"+String.valueOf(filter)+"App", app);
		edit.putBoolean("filter"+String.valueOf(filter)+"Popup", popupAllowed);
		edit.putBoolean("filter"+String.valueOf(filter)+"Expansion", expansionAllowed);
		edit.commit();
	}
	
	protected void editFilter(int filter, String app, boolean popupAllowed, boolean expansionAllowed, String[] keywords){
		for( int i = 0 ; i < prefs.getInt("filter"+String.valueOf(filter)+"numberOfKeywords", 0) ; i++ ){
			edit.remove("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(i));
		}
		edit.remove("filter"+String.valueOf(filter)+"numberOfKeywords");
		edit.putString("filter"+String.valueOf(filter)+"App", app);
		edit.putBoolean("filter"+String.valueOf(filter)+"Popup", popupAllowed);
		edit.putBoolean("filter"+String.valueOf(filter)+"Expansion", expansionAllowed);
		int pos = 0;
		for( int i = 0 ; i < keywords.length ; i++ ){
			if( !isUseless(keywords, i) ){
				edit.putString("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(pos++), cleaned(keywords[i]));
			}
		}
		edit.putInt("filter"+String.valueOf(filter)+"numberOfKeywords", pos);
		edit.commit();
	}
	
	private String cleaned(String keyword) {
		boolean startsWithCharacter = false;
		while( !startsWithCharacter ){
			if( keyword.indexOf(" ") == 0 )
				keyword = keyword.substring(1);
			else
				startsWithCharacter = true;
		}
		if( keyword.equals("") )
			return "";
		boolean endsWithCharacter = false;
		while( !endsWithCharacter ){
			if( keyword.lastIndexOf(" ") == keyword.length()-1 )
				keyword = keyword.substring(0, keyword.length()-1);
			else
				endsWithCharacter = true;
		}
		return keyword;
	}

	private boolean isUseless(String[] keywords, int position){
		String keyword = cleaned(keywords[position]);
		if( keyword.equals("") )
			return true;
		for( int i = 0 ; i < position ; i++ ){
			if( cleaned(keywords[i]).equals(keyword) )
				return true;
		}
		return false;
	}
	
	protected void removeFilter(int filter){
		edit.remove("filter"+String.valueOf(filter)+"App");
		edit.remove("filter"+String.valueOf(filter)+"Popup");
		edit.remove("filter"+String.valueOf(filter)+"Expansion");
		for( int i = 0 ; i < prefs.getInt("filter"+String.valueOf(filter)+"numberOfKeywords", 0) ; i++ ){
			edit.remove("filter"+String.valueOf(filter)+"Keyword"+String.valueOf(i));
		}
		edit.remove("filter"+String.valueOf(filter)+"numberOfKeywords");
		for( int i = filter + 1 ; i < getNumberOfFilters() ; i++ ){
			edit.putString("filter"+String.valueOf(i-1)+"App", getFilterApp(i));
			edit.remove("filter"+String.valueOf(i)+"App");
			edit.putBoolean("filter"+String.valueOf(i-1)+"Popup", isPopupAllowed(i));
			edit.remove("filter"+String.valueOf(i)+"Popup");
			edit.putBoolean("filter"+String.valueOf(i-1)+"Expansion", isExpansionAllowed(i));
			edit.remove("filter"+String.valueOf(i)+"Expansion");
			for( int j = 0 ; j < prefs.getInt("filter"+String.valueOf(i)+"numberOfKeywords", 0) ; j++ ){
				edit.putString("filter"+String.valueOf(i-1)+"Keyword"+String.valueOf(j), prefs.getString("filter"+String.valueOf(i)+"Keyword"+String.valueOf(j), ""));
				edit.remove("filter"+String.valueOf(i)+"Keyword"+String.valueOf(j));
			}
			edit.putInt("filter"+String.valueOf(i-1)+"numberOfKeywords", prefs.getInt("filter"+String.valueOf(i)+"numberOfKeywords", 0));
			edit.remove("filter"+String.valueOf(i)+"numberOfKeywords");
		}
		edit.putInt( "numberOfFilters", getNumberOfFilters() - 1 );
		edit.commit();
	}
}
