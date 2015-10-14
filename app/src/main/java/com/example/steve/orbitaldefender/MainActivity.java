package com.example.steve.orbitaldefender;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/* OVERVIEW:
    The MainActivity class takes care of setting up the fullscreen and handling the android UI.
    It notifies the Core view when the app is out of or in focus so it stops and starts up the animation
    thread properly.
 */

public class MainActivity extends Activity {

    //either GestureDetectorCompat or GestureDetector can be used but seems GestureDectorCompat its the latest
    //GestureDetector comes with some predefined gestures so we don't need to process all the raw data from a motionEvent.
    private GestureDetectorCompat gestureDetector;
    private CoreView coreView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenSize screenSize = new ScreenSize(this, this.getWindow().getWindowManager().getDefaultDisplay());
        coreView = new CoreView(this, screenSize);
        setContentView(coreView);

        //embedding the listener here allows us to directly use the activity's private methods
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener(){
            //first touch event that is called is onDown() before all others, if return false all other listener methods get ignored
            //returning true tells the system that the method will handle the event, otherwise it gets passed down the View hierarchy
            public boolean onDown(MotionEvent e) { return true; }

            //onSingleTapConfirmed makes sure its just a single tap, not followed by a double tap.
            public boolean onSingleTapConfirmed(MotionEvent e) {
                hideSystemUI();
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    //onResume() is called whenever the app regains focus
    protected void onResume(){
        super.onResume();
        hideSystemUI();
        if (!coreView.isGamePaused()) //only resume game if there wasn't a manual pause prior to losing focus
            coreView.resume();
    }

    //onPause() is called whenever the app loses focus (e.g. user interacts with the navigation bar or status bar, a dialog box pops up)
    //any heavy saving should be done in onStop() (app is hidden), not onPause().
    protected void onPause(){
        super.onPause();
        showSystemUI();
        if (coreView.isRunning())
            coreView.pause();
    }

    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event); //must pass this motion event to the gestureDetector.
        return super.onTouchEvent(event);
    }

    private void hideSystemUI(){
        View view = getWindow().getDecorView();
        int UIoptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN //makes sure sub-views stay in place and don't get resized
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //makes sure sub-views stay in place and don't get resized
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
        view.setSystemUiVisibility(UIoptions);
    }

    private void showSystemUI(){
        View view = getWindow().getDecorView();
        int UIoptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        view.setSystemUiVisibility(UIoptions);
    }
}
