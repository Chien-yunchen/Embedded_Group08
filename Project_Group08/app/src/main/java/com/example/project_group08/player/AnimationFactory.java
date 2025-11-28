package com.example.project_group08.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.project_group08.R;

public class AnimationFactory {
    public static Animation createRunAnimation(Context ctx) {
        return new Animation(
                new Bitmap[]{
                        BitmapFactory.decodeResource(ctx.getResources(), R.drawable.run1),
                        BitmapFactory.decodeResource(ctx.getResources(), R.drawable.run2)
                },
                120 // 每 120ms 換一張
        );
    }

    public static Animation createJumpAnimation(Context ctx){
        return new Animation(
                new Bitmap[]{
                        BitmapFactory.decodeResource(ctx.getResources(), R.drawable.jump)
                },
                200 // 單一張就不會循環，但這樣寫比較一致
        );
    }

    public static Bitmap getCandyBitmap(Context context) {
        return BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.candy  // candy.png
        );
    }
}
