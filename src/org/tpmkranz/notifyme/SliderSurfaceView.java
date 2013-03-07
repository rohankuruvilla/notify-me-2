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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SliderSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

	protected boolean onDisplay;
	protected SurfaceHolder sHolder;
	private Bitmap lock, handle, dismiss, dismiss0, view, view0;
	private Canvas canvas;
	protected float centerY, centerX, leftX, rightX, drawX, offsetX, offsetY;
	private int color;
	Prefs prefs;
	
	public SliderSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		prefs = new Prefs(context);
		onDisplay = false;
		color = Color.rgb(prefs.getSliderBackgroundR(), prefs.getSliderBackgroundG(), prefs.getSliderBackgroundB());
		sHolder = this.getHolder();
		sHolder.addCallback(this);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		onDisplay = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		onDisplay = false;
	}

	protected void setDimensions(float width, float height){
		centerX = width / 2;
		centerY = height / 2;
		leftX = centerX - 3*width/8;
		rightX = centerX + 3*width/8;
	}
	
	protected void setBitmaps(Bitmap l, Bitmap h, Bitmap v, Bitmap v0, Bitmap d, Bitmap d0){
		lock = l;
		handle = h;
		view = v;
		view0 = v0;
		dismiss = d;
		dismiss0 = d0;
		offsetX = l.getWidth() / 2;
		offsetY = l.getHeight() / 2;
	}
	
	protected double dist(float[] a, float[] b){
		return Math.sqrt( Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2) );
	}
	
	protected void doDraw(float x, boolean touch){
		canvas = sHolder.lockCanvas();
		if( canvas == null )
			return;
		if( x < centerX ){
			drawX = ( x < leftX ? leftX : x );
		}else{
			drawX = ( x > rightX ? rightX : x );
		}
		canvas.drawColor(color);
		if( drawX == leftX )
			canvas.drawBitmap(dismiss0, leftX - offsetX, centerY - offsetY, null);
		else
			canvas.drawBitmap(dismiss, leftX - offsetX, centerY - offsetY, null);
		if( drawX == rightX )
			canvas.drawBitmap(view0, rightX - offsetX, centerY - offsetY, null);
		else
			canvas.drawBitmap(view, rightX - offsetX, centerY - offsetY, null);
		if( !touch )
			canvas.drawBitmap(lock, drawX - offsetX, centerY - offsetY, null);
		else if( drawX != leftX && drawX != rightX )
			canvas.drawBitmap(handle, drawX - offsetX, centerY - offsetY, null);
		sHolder.unlockCanvasAndPost(canvas);
	}
}
