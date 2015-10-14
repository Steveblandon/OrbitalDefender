package com.example.steve.orbitaldefender;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

//simple class for compatibility when getting screen dimensions
public class ScreenSize {

    private int width;
    private int height;

    /*OVERVIEW:
        getRealSize() gets the actual size of the display, which is exactly whats needed for a fullscreen app.
        getDisplayMetrics() on the other hand gets the available size of the display assuming the navigation bar is present.
        Therefore when using metrics one must get the navigation bar height to get actual screen size (if fullscreen).
        getRealSize() takes care of all that but requires a higher API (API 17).
     */
    public ScreenSize(Context context, Display display){
        //cleaner, simpler, anyone using API level 17 or higher uses this one
        if (Build.VERSION.SDK_INT >= 17){
            Point point = new Point();
            display.getRealSize(point);
            width = point.x;
            height = point.y;
        }
        else{
            //this can be used with any API, less clean and efficient (because of the identifier search) but gets the job done
            DisplayMetrics metrics = new DisplayMetrics();
            Resources res = context.getResources();
            int resourceId =  res.getIdentifier("navigation_bar_height", "dimen", "android");
            int navBar_height = 0;
            if (resourceId > 0) navBar_height = res.getDimensionPixelSize(resourceId);
            height = metrics.heightPixels + navBar_height;
            width = metrics.widthPixels;
        }
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
}
