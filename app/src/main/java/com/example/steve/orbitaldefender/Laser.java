package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;

import java.util.List;

public class Laser extends WorldObject {

    public final static String TAG  = Laser.class.getSimpleName();
    private float direction;
    private static final float speed = 120;

    public Laser(Bitmap bitmap, float cx, float cy){
        super(bitmap, cx - bitmap.getWidth() / 2, cy - bitmap.getHeight() / 2);
    }

    public void move(){
        setLocation(getX() + speed * (float) Math.cos(Math.toRadians(direction - 90)),
                getY() + speed * (float) Math.sin(Math.toRadians(direction - 90)));
    }

    public void rotate(){
        //rotate bitmap if need be
        getMatrix().reset(); //without this, postTranslate() would cause the bitmap to slide across the screen
        getMatrix().postRotate(direction, getBitmap().getWidth() / 2, getBitmap().getHeight() / 2);
        getMatrix().postTranslate(getX(), getY());
    }

    public void setDirection(float angle){
        //set direction for movement
        direction = angle;
    }

    public Asteroid intersectsAsteroid(List<WorldObject> objects){
        //collision detection; return any asteroid object it intersects with
        for (WorldObject object : objects)
            if (object instanceof Asteroid && object.isVisible()){
                //try to get the distance from asteroid to bullet, and do intersection with that
                double dist = Math.sqrt( (Math.pow(object.getCenterX() - getCenterX(), 2)
                        + Math.pow(object.getCenterY() - getCenterY(), 2) ));
                double dist2 = Math.sqrt( (Math.pow(object.getCenterX() - getCenterX(), 2)
                        + Math.pow(object.getCenterY() - (getY() + getBitmap().getHeight()/7), 2) ));
                //intersects at either the tip or center or laser
                if (dist <= object.getBitmap().getWidth() / 2 || dist2 <= object.getBitmap().getWidth() / 2){
                    return (Asteroid) object;
                }
            }
        return null; //if there was no collision
    }

    public void enable(float cx, float cy, boolean reset){
        super.enable(cx - getBitmap().getWidth() / 2, cy - getBitmap().getHeight() / 2, reset);
    }
}
