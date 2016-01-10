package com.example.steve.orbitaldefender;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.provider.MediaStore;

import com.example.steve.basicgame.R;

import java.util.HashMap;

//queue sounds, stop sound if its playing so it can start from the beginning, restart sounds, etc.
public class SoundManager {

    private HashMap<String, Integer> sounds;
    private HashMap<String, Integer> isPlaying;
    private Context context;
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;

    public SoundManager(Context context){
        sounds = new HashMap<>();
        this.context = context;

        int maxStreams = 3; //number of sounds that can be playing at the same time (the more, the more mixing, the more CPU use)
        if (Build.VERSION.SDK_INT >= 21){
            //this way seems to be the new and improved, more detailed way of setting up a SoundPool
            AudioAttributes.Builder attBuilder = new AudioAttributes.Builder();
            attBuilder.setUsage(AudioAttributes.USAGE_GAME);
            attBuilder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
            attBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(attBuilder.build());
            builder.setMaxStreams(maxStreams);
            soundPool = builder.build();
        }
        else soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0); //for compatibility (otherwise would have to set min to API 22)
    }

    public void load(String tag, int resId){
        int soundID = soundPool.load(context,resId, 0);
        sounds.put(tag,soundID);
    }

    public void load(String tag, int resId, int priority){
        int soundID = soundPool.load(context,resId, priority);
        sounds.put(tag, soundID);
    }

    public void play(String tag){
        //this method is simply to avoid having to set all these default parameters and just play the sound

        /* from SoundPool.play() documentation for reference purposes:
        soundID     a soundID returned by the load() function
        leftVolume  left volume value (range = 0.0 to 1.0)
        rightVolume right volume value (range = 0.0 to 1.0)
        priority	stream priority (0 = lowest priority)
        loop	    loop mode (0 = no loop, -1 = loop forever)
        rate    	playback rate (1.0 = normal playback, range 0.5 to 2.0)
         */
        soundPool.play(sounds.get(tag), 1, 1, 0, 0, 1);
    }

    public void playMusic(int resId){
        if (mediaPlayer == null) mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void stopMusic(){
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
    }
}
