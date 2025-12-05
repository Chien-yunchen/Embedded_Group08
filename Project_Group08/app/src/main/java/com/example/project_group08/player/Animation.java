package com.example.project_group08.player;

import android.graphics.Bitmap;

public class Animation {
    private Bitmap[] frames;
    private int frameIndex = 0;
    private long frameTime;
    private long lastFrameChange;

    public Animation(Bitmap[] frames, long frameTime) {
        this.frames = frames;
        this.frameTime = frameTime;
        this.lastFrameChange = System.currentTimeMillis();
    }

    public void update(){
        long now = System.currentTimeMillis();
        if (now - lastFrameChange > frameTime) {
            frameIndex = (frameIndex + 1) % frames.length;
            lastFrameChange = now;
        }
    }

    public Bitmap getFrame(){
        return  frames[frameIndex];
    }


}
