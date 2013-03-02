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

import android.app.Application;
import android.os.Parcelable;

public class TemporaryStorage extends Application {
	private static Parcelable storedP;
	private static int filter;
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	public void storeStuff(Parcelable parc){
		storedP = parc;
	}
	
	public void storeStuff(int filt){
		filter = filt;
	}
	
	public Parcelable getParcelable(){
		return storedP;
	}
	
	public int getFilter(){
		return filter;
	}
	
}
