package com.example.steve.orbitaldefender;

import android.graphics.Paint;
import android.graphics.Typeface;

public class InstantScore extends WorldObject{
    //a class that has almost the same effect as a particle, it appears at the center of a hit asteroid and goes up as it fades
    //it displays how many points were gained from hitting that asteroid

    private int scorePts;
    private final int direction = -90;
    private final float speed = 1;
    private Paint paint; //color property to handle its color and transparency (alpha)
    private int age, decay; //age goes up every tick, decay is rate by which particle fades away
    private final int lifespan = 30; //number of ticks before completely fading out

    public InstantScore(int scorePts, float x, float y, int color, ObjectManager objectManager) {
        //bitmap parameter should be null
        super(null, x, y);
        
        this.scorePts = scorePts;
        age = 0;
        decay = 255 / lifespan;

        paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(paint.getTextSize() * 5);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAlpha(255);

        objectManager.add(this);
    }

    public void update(){
        if (age < lifespan){
            float x = getX();
            float y = getY();
            if (age > 0){
                x += Math.cos(Math.toRadians(direction)) * speed;
                y += Math.sin(Math.toRadians(direction)) * speed;
                if (paint.getAlpha() > 0) paint.setAlpha(paint.getAlpha() - decay);
            }
            setLocation(x, y);
            age++;
        }
        else {
            disable(); //disable object once it has faded away and reached its lifespan
        }
    }

    public void enable(int scorePts, float x, float y){
        super.enable(x, y, false);
        this.scorePts = scorePts;
        age = 0;
    }

    public Paint getPaint(){ return paint; }
    public String getScorePts(){
        if (scorePts > 0)
            return "+" + scorePts;
        else return "" + scorePts;
    }
}
