package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;
import android.graphics.Matrix;


//generic object with graphical representation on screen, all game objects are sub classes to this class
public abstract class WorldObject {

    public final static String TAG  = WorldObject.class.getSimpleName();
    private float x, y; //coordinates
    private boolean isVisible;
    private Bitmap bitmap;
    private Matrix matrix;

    public WorldObject(Bitmap bitmap, float x, float y){
        isVisible = true;
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        matrix = new Matrix();

    }

    public void enable(float x, float y, boolean reset){
        isVisible = true;
        setLocation(x, y); //must set new location when enabling
        if (reset){ //this is only for objects with graphical representation that rotate
            getMatrix().reset(); //must set matrix as well otherwise there is a graphic glitch on the first frame after enabling object
            getMatrix().setTranslate(x,y);
        }
    }

    public void disable(){
        //makes the object no longer visible so its not longer updated or drawn on the screen
        //it could be re-purposed, recalled, or made collectible by the GC at a later time.
        isVisible = false;
    }

    public void setLocation(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    //GETTERS
    public Bitmap getBitmap(){ return bitmap; }
    public Matrix getMatrix(){ return  matrix; }
    public float getX(){ return x; }
    public float getY(){ return y; }
    public float getCenterX(){ return getX() + bitmap.getWidth() / 2; }
    public float getCenterY(){ return getY() + bitmap.getHeight() / 2; }
    public boolean isVisible(){ return isVisible; }
}
