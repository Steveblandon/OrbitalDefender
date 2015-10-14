package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;

import java.util.List;

public class Planet extends WorldObject {

    public final static String TAG  = Planet.class.getSimpleName();
    public final static String glow_TAG  = "earth_glow";
    private final int speed = 1; //scrolling speed

    public Planet(Bitmap bitmap, float x, float y) {
        super(bitmap, x, y);
    }

    public void scroll(ScreenSize screenSize){
        //scrolls image horizontally (moving to the right)
        if (getX() < screenSize.getWidth()) setLocation(getX()+speed, getY()); //move forward
        else if (getX() >= screenSize.getWidth()) setLocation(0, getY()); //reset location
    }

    public Asteroid intersectsAsteroid(List<WorldObject> objects){
        //collision detection; return any asteroid object it intersects with
        for (WorldObject object : objects)
            if (object instanceof Asteroid && object.isVisible()){
                //try to get the distance from asteroid to planet, since the planet is a rectangular strip, only use the y coordinate
                double dist = getCenterY() - object.getCenterY();
                if (dist < getBitmap().getHeight() / 2){
                    return (Asteroid) object;
                }
            }
        return null;
    }
}
