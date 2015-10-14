package com.example.steve.orbitaldefender;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.HashMap;

public class GraphicsManager {

    private HashMap<String, Bitmap> images;
    private HashMap<String, SheetAnimation> animations;
    private Context context;
    private Bitmap bgd; //background image
    private float bgdX;
    private Matrix matrix; //generic default matrix

    public GraphicsManager(Context context){
        this.context = context;

        images = new HashMap<>();
        animations = new HashMap<>();
        matrix = new Matrix();
    }

    public void load(String tag, int resid){
        images.put(tag, BitmapFactory.decodeResource(context.getResources(), resid));
    }

    public void load(String tag, int resid, float scaleMult){ //scales image
        if (scaleMult == 0 ) scaleMult = 1; //so image doesn't get scaled but doesn't end up with zero width/height
        scaleMult = Math.abs(scaleMult);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resid);
        images.put(tag, Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scaleMult), (int) (bitmap.getHeight() * scaleMult), true));
    }

    public void load(String tag, int resid, int width, float scaleMult){ //stretches/shrinks image horizontally to match given width
        //applies scaleMult only to the height (optional)
        if (scaleMult == 0 ) scaleMult = 1; //so image doesn't get scaled but doesn't end up with zero width/height
        scaleMult = Math.abs(scaleMult);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resid);
        images.put(tag, Bitmap.createScaledBitmap(bitmap, width, (int) (bitmap.getHeight() * scaleMult), true));
    }

    public void loadBgd(int resid, ScreenSize screenSize){ //load a background image
        bgd = BitmapFactory.decodeResource(context.getResources(), resid);
        bgd = Bitmap.createScaledBitmap(bgd, screenSize.getWidth(), screenSize.getHeight(), true); //automatically scale to display parameters
    }

    public void scrollBgd(float speed, int screenWidth){
        //just a horizontal scroll
        if (bgdX < screenWidth)
            bgdX += speed;
        else bgdX = 0; //reset
    }

    //GETTERS
    public Bitmap get(String tag){
        return images.get(tag);
    }
    public Bitmap getBgd(){ return bgd; }
    public float getBgdX(){ return bgdX; }
    public SheetAnimation getAnimation(String tag) {
        //gets the animation associated with the given tag
        return animations.get(tag); //returns null if it doesn't exist
    }




    //handles horizontal sprite sheet animations
    public class SheetAnimation {

        private Bitmap[] frames; //must split up spritesheet to be able to rotate each frame through a matrix when drawn
        private boolean active; //whether or not the animation object is active (deactivates when animation is complete, last frame reached)
        private int imgHeight; //height of the sprite sheet
        private int fWidth; //frame width, each frame in the sprite sheet should have the same width
        private int frameCount, curFrame, curX;
        private int frame_period; //number of ticks to skip before moving to the next frame

        public SheetAnimation(String tag, Bitmap spriteSheet, int frameCount, int MAX_FPS, int framePeriod){
            //assumes all frames are of the same width
            imgHeight = spriteSheet.getHeight();
            this.frameCount = frameCount;
            fWidth = spriteSheet.getWidth()/frameCount;
            frame_period = MAX_FPS/frameCount; //slowest frame period
            if (framePeriod < 0) frame_period = 1; //no tick skips
            else if (framePeriod < frame_period) frame_period = framePeriod; //somewhere in between
            active = false;

            //cut up spritesheet
            this.frames = new Bitmap[frameCount];
            for (int i = 0; i < frameCount; i++){
                curX = 0 + fWidth * curFrame;
                this.frames[i] = Bitmap.createBitmap(spriteSheet,curX,0,fWidth,imgHeight);
                curFrame++;
            }
            curX = 0;
            curFrame = 0;

            animations.put(tag, this);
        }

        public void update(int ticks){ //ticks represents the current number of updates that have been done per second (UPS count)
            if (curFrame < frameCount && ticks % frame_period == 0 && active) {
                curX = 0 + fWidth * curFrame;
                curFrame++;
            }
            if (curFrame == frameCount){
                curFrame = 0;
                active = false; //no looping, must call play() again
            }
        }

        public Matrix rotate(float angle, float pivot, float x, float y, float offset){
            //returns a matrix with rotation to apply to any frame
            matrix.reset();
            matrix.postRotate(angle, fWidth / 2, (imgHeight * pivot));
            matrix.postTranslate(x, y - (float) Math.ceil(imgHeight * offset));
            return matrix;
        }

        public void play(){ active = true; }

        //GETTERS:
        public Bitmap getFrame(){ return frames[curFrame]; }
        public boolean isPlaying(){ return active; }
    }
}
