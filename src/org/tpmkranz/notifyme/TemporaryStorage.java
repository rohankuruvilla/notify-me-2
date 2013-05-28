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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.os.Parcelable;
import android.util.Log;

public class TemporaryStorage extends Application {
	private static List<Parcelable> storedP;
	private static Map<Integer, Parcelable> storedPMap;
	private static Map<String, Parcelable> storedPMapNames;

	private static List<Integer> filters;
	private static List<String> names, namesIdxs;
	private static long timeout;
	private static boolean access;
	private static int idx;
	private boolean isLongPress;
	private boolean isBig;
	@Override
	public void onCreate(){
		super.onCreate();
		isBig = true;
		filters = new ArrayList<Integer>();
		storedP = new ArrayList<Parcelable>();
		names = new ArrayList<String>();
		namesIdxs = new ArrayList<String>();

		storedPMap = new HashMap<Integer, Parcelable>();
		storedPMapNames = new HashMap<String, Parcelable>();

		access = false;
		idx = 0;
	}
	
	public boolean getIsBig() {
		return isBig;
	}
	
	public void setIsBig(boolean big) {
		isBig = big;
	}
	public void storeStuff(Parcelable parc){
		storedP.add(parc);		
	}
	
	public void storeStuff(int filt){
		filters.add(filt);
	}
	
	public void removeParcelable(int index) {
		storedPMapNames.remove(namesIdxs.get(index));
		for(int i = 0; i < names.size(); ++i) {
			if(names.get(i).equalsIgnoreCase(namesIdxs.get(index))) {
				Log.i("TESTING", "removing name: "+namesIdxs.get(index));
				names.remove(i);
			}
		}
		createList();
	}
	
	
	private void createList() {
		namesIdxs.clear();
		List<Parcelable> theList = new ArrayList<Parcelable>();
		for(int i = 0; i < names.size(); ++i) {
			theList.add((Parcelable) storedPMapNames.get(names.get(i)));
			namesIdxs.add(names.get(i));
	    }
	
		storedP.clear();
		storedP = theList;
	}
	
	private void createList(Parcelable parc, String name) {
		namesIdxs.clear();
		List<Parcelable> theList = new ArrayList<Parcelable>();
		for(int i = 0; i < names.size(); ++i) {
			if(!names.get(i).equalsIgnoreCase(name)) {
				theList.add((Parcelable) storedPMapNames.get(names.get(i)));
				namesIdxs.add(names.get(i));
	        }
	    }
		namesIdxs.add(0, name);
		theList.add(0, parc);
		storedP.clear();
		storedP = theList;
	}
	
	public void setLongPress(boolean isLong) {
		isLongPress = isLong;
	}
	
	public boolean getLongPress() {
		return isLongPress;
	}
	
	public void storeStuff(String name, Parcelable parc, int filter){
		//removeParcelable(parc);
		if(storedPMapNames.put(name, parc) == null) {
			storeStuff(parc);
			storeStuff(name);
			storeStuff(filter);
		}
		
		createList(parc, name);
		
		Log.i("TESTING", "List size: "+storedP.size());
	}
	
	
	public void storeStuff(long time){
		timeout = time;
	}
	
	public void storeStuff(String name){
		names.add(name);;
	}
	
	
	public void setCurrentIndex(int index) {
		idx = index;
	}
	
	public int getCurrentIndex() {
		return idx;
	}
	
	public List<Parcelable> getParcelable(){
		return storedP;
	}
	
	public int getFilter(){
		return filters.get(0);
	}
	
	public Map<Integer, Parcelable> getParcelableMap() {
		return storedPMap;
	}
	
	public long getTimeout(){
		return timeout;
	}
	
	public void accessGranted(boolean granted){
		access = granted;
	}
	
	public boolean hasAccess(){
		return access;
	}
}
