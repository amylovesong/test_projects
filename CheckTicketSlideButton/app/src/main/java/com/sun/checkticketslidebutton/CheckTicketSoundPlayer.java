package com.sun.checkticketslidebutton;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;

/**
 * @author sxl  (sunxiaoling@didichuxing.com)
 * @date 2017/5/16 20:17
 */
public class CheckTicketSoundPlayer {
    private final String SOUND_SLIDE = "slide";
    private final String SOUND_CHECKED = "checked";
    private final String SOUND_UNCHECKED = "unchecked";

    private final Context mContext;
    private SoundPool mSoundPool;
    //定义一个HashMap用于存放音频流的ID
    private HashMap<String, Integer> mSounds = new HashMap<>();

    public CheckTicketSoundPlayer(Context context) {
        this.mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        this.initialize();
    }

    private void initialize() {
        this.mSounds.put(SOUND_SLIDE, mSoundPool.load(this.mContext, R.raw.sofa_check_ticket_slide, 1));
        this.mSounds.put(SOUND_CHECKED, mSoundPool.load(this.mContext, R.raw.sofa_check_ticket_success, 1));
        this.mSounds.put(SOUND_UNCHECKED, mSoundPool.load(this.mContext, R.raw.sofa_check_ticket_cancel, 1));
    }

    public void playSlideSound() {
        playSound(SOUND_SLIDE);
    }

    public void playCheckedSound(boolean checked) {
        final String soundName = checked ? SOUND_CHECKED : SOUND_UNCHECKED;
        playSound(soundName);
    }

    private void playSound(String soundName) {
        this.mSoundPool.play(this.mSounds.get(soundName), 1, 1, 1, 0, 1f);
    }
}
