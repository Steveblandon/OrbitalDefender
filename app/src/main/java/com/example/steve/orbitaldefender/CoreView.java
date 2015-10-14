package com.example.steve.orbitaldefender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CoreView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private boolean isRunning;
    private SurfaceHolder holder;
    private Thread displayThread;
    private LogicControl control;
    private Paint paint;
    private ScreenSize screenSize;
    private int ticks; //counts every game update
    private int ticksOnHold; //for pause delays
    private boolean pauseGame; //a trigger for pausing game at the end of a cycle


    public CoreView(Context context, ScreenSize screenSize) {
        super(context);
        control = new LogicControl(this.getContext(), this, screenSize);
        this.screenSize = screenSize;

        //initialize paint for drawing FPS text on screen
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        float textSize = paint.getTextSize();
        paint.setTextSize(textSize * 4); //scale text

        //register holder so we can lock canvas
        holder = getHolder();
        holder.addCallback(this);
    }

    //process input on main UI thread
    public boolean onTouchEvent(MotionEvent event){
        control.inputHandler(event);
        return true;
    }

    //update game logic and render graphics on a separate thread
    public void run() {

        final int MAX_FPS = 30; //desired frame rate
        final double FRAME_PERIOD = 1000/MAX_FPS; //ideal time each loop should take
        final int MAX_FRAME_SKIPS = 5;
        int timeElapsed = 0, FPS_count = 0, FPS = 0, framesSkipped = 0; //for measurement purposes
        long frameTime = 0; //for measurement purposes

        while(isRunning){
            //make sure the surface has been created before continuing
            if (!holder.getSurface().isValid()) continue; //skips loop until surface is created

            long startTime = SystemClock.currentThreadTimeMillis();
            //update logic
            control.update(ticks);
            tick();
            //render graphics
            Canvas canvas = holder.lockCanvas();
            control.render(canvas);
            canvas.drawText("FPS:" + FPS, (int) (screenSize.getWidth() * .8), screenSize.getHeight() - 50, paint);
            holder.unlockCanvasAndPost(canvas);
            //^

            //stabilize frame rate
            double deltaTime = SystemClock.currentThreadTimeMillis() - startTime;
            int sleepTime = (int) (FRAME_PERIOD - deltaTime);
            if (sleepTime >= 0) { //loop went too fast, slow down
                try{
                    displayThread.sleep(sleepTime); //let CPU rest, good for battery saving
                } catch (InterruptedException e) {  }
            }
            else {
                framesSkipped = 0; //reset framesSkipped
                while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) { //loop took too long, need to catch up
                    //update game logic but skip rendering
                    control.update(ticks);
                    tick();
                    sleepTime += FRAME_PERIOD;
                    framesSkipped++; //eventually to make sure not too many frames are skipped in one loop (otherwise game might be unplayable)
                }
            }

            //measure frame rate
            if (timeElapsed < 1000) {
                timeElapsed += SystemClock.elapsedRealtime() - frameTime;
                FPS_count +=1;
            }
            frameTime = SystemClock.elapsedRealtime();
            if (timeElapsed >= 1000) { //update FPS on screen every second
                FPS = FPS_count;
                FPS_count = 0;
                timeElapsed = 0;
                ticks = 0; //reset so it doesn't increase infinitely
            }
            if (pauseGame) pause();
        }
    }

    private void tick(){
        ticks++;
        //take care of delayed pauses
        if (ticksOnHold > 0){
            ticksOnHold--;
            if (ticksOnHold <= 0){
                ticksOnHold = 0; //reset
                pauseGame = true; //activate trigger
            }
        }
    }

    public void resume(){
        isRunning = true;
        pauseGame = false;
        //start drawing thread
        displayThread = new Thread(this);
        displayThread.start();
    }

    public void pause(){
        isRunning = false;
        boolean retry = true;
        while (retry){
            try{ //keep trying to block thread until it shuts off successfully
                displayThread.join();
                retry = false;
            } catch (InterruptedException e){   }
        }
    }

    public void delayedPause(int ticks){
        //waits a set amount of ticks before pausing
        ticksOnHold = ticks;
    }

    public void surfaceCreated(SurfaceHolder holder) {  }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {    }
    public void surfaceDestroyed(SurfaceHolder holder) {    }

    public boolean isRunning(){ return isRunning; }
    public boolean isGamePaused(){ return pauseGame; }
}
