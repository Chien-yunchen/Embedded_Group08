package com.example.project_group08.player;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Sprite {
    private Animation animation;
    public Sprite(Animation animation){
        this.animation = animation;
    }

    // 允許 A 更換動畫（例如從跑步 → 跳躍）
    public void setAnimation(Animation animation){
        this.animation = animation;
    }

    public void update(){
        animation.update();
    }

    public void draw(Canvas canvas, float x, float y){
        Bitmap frame = animation.getFrame();
        canvas.drawBitmap(frame, x, y, null);
    }
}
