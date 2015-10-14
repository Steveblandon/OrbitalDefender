package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;

import java.util.List;

//this is the main game object that responds directly to user input
public class Spaceship extends WorldObject {

    public final static String TAG  = Spaceship.class.getSimpleName();
    private float lastX;
    private float lastAngle, curAngle, targetAngle;
    private final float maxAngle = 70;
    private int coolDown;
    private final int COOLDOWN = 15; //number of steps before it can shoot again (should be less than MAX_FPS)

    public Spaceship(Bitmap bitmap, float x, float y){
        super(bitmap, x, y);

        getMatrix().reset();
        getMatrix().setTranslate(x, y); //sets default coordinates
        coolDown = COOLDOWN;
    }

    public void setAngle(float x){ //parameters hold coordinates to aim to
        float angle = (float) Math.atan2(500, lastX - x);
        angle = (float) Math.toDegrees(angle) - 90 + lastAngle;
        if (angle > -maxAngle && angle < maxAngle){
            curAngle = angle;
        }
    }

    public void rotate(){
        if (curAngle != lastAngle){
            getMatrix().reset();
            getMatrix().postRotate(curAngle, getBitmap().getWidth() / 2, getBitmap().getHeight() / 2);
            getMatrix().postTranslate(getX(), getY());
        }
    }

    public void shoot(Laser bullet){
        bullet.setDirection(curAngle);
        resetCoolDown();
    }

    public Asteroid intersectsAsteroid(List<WorldObject> objects){
        //collision detection; return any asteroid object it intersects with
        for (WorldObject object : objects)
            if (object instanceof Asteroid && object.isVisible()){
                //try to get the distance from asteroid to bullet, and do intersection with that
                double dist = Math.sqrt( (Math.pow(object.getCenterX() - getCenterX(), 2)
                        + Math.pow(object.getCenterY() - getCenterY(), 2) ));
                if (dist < object.getBitmap().getWidth()/2 + getBitmap().getWidth()/2){
                    return (Asteroid) object;
                }
            }
        return null; //if there was no collision
    }

    //to delay shooting and allow some time for shooting animations and sounds to play off
    public void coolDown(){ if (coolDown > 0) coolDown--; }
    private void resetCoolDown(){ coolDown = COOLDOWN; }

    public void setLastX(float x){ lastX = x; }

    public void setLastAngle(float x){
        float ang = (float) Math.atan2(500, lastX - x);
        lastAngle = (float) Math.toDegrees(ang) - 90 + lastAngle;
        if (lastAngle > maxAngle) lastAngle = maxAngle;
        else if (lastAngle < -maxAngle) lastAngle = -maxAngle;
    }

    //GETTERS:
    public float getCurAngle(){ return curAngle; }
    public int getTimetoCoolDown() { return COOLDOWN; }
    public int getCoolDownTime() {return coolDown; }
    public boolean isReady(){ if (coolDown == 0) return true; else return false; }
}
