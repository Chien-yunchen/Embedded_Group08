package com.example.project_group08.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;

public class StartMenuUI {
    private boolean isStarted = false;

    private int screenWidth = 800;
    private int screenHeight = 1200;

    // 繪圖工具
    private Paint backgroundPaint;
    private Paint titlePaint;
    private Paint titleShadowPaint;
    private Paint buttonPaint;
    private Paint buttonBorderPaint;
    private Paint buttonTextPaint;
    private Paint hintPaint;

    // 開始按鈕
    private RectF startButton;

    // 動畫相關
    private float animationTime = 0;

    public StartMenuUI(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // 背景（漸層藍色）
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(135, 206, 235));  // 天空藍
        backgroundPaint.setStyle(Paint.Style.FILL);

        // 遊戲標題（大字）
        titlePaint = new Paint();
        titlePaint.setColor(Color.rgb(255, 215, 0));  // 金色
        titlePaint.setTextSize(180);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        titlePaint.setShadowLayer(15, 5, 5, Color.rgb(139, 69, 19));  // 棕色陰影

        // 標題陰影（描邊效果）
        titleShadowPaint = new Paint();
        titleShadowPaint.setColor(Color.rgb(139, 69, 19));  // 棕色
        titleShadowPaint.setTextSize(180);
        titleShadowPaint.setTextAlign(Paint.Align.CENTER);
        titleShadowPaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        titleShadowPaint.setStyle(Paint.Style.STROKE);
        titleShadowPaint.setStrokeWidth(8);

        // 開始按鈕（粉紅色，類似餅乾風格）
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.rgb(255, 105, 180));  // 粉紅色
        buttonPaint.setStyle(Paint.Style.FILL);

        // 按鈕邊框（白色描邊）
        buttonBorderPaint = new Paint();
        buttonBorderPaint.setColor(Color.WHITE);
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(8);

        // 按鈕文字
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(90);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        buttonTextPaint.setShadowLayer(5, 2, 2, Color.BLACK);

        // 提示文字
        hintPaint = new Paint();
        hintPaint.setColor(Color.WHITE);
        hintPaint.setTextSize(40);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setAlpha(200);

        // 開始按鈕位置（畫面中央偏下）
        float buttonCenterX = screenWidth / 2f;
        float buttonCenterY = screenHeight / 2f + 150;
        float buttonWidth = 350;
        float buttonHeight = 150;

        startButton = new RectF(
                buttonCenterX - buttonWidth / 2,
                buttonCenterY - buttonHeight / 2,
                buttonCenterX + buttonWidth / 2,
                buttonCenterY + buttonHeight / 2
        );
    }

    /**
     * 更新動畫（可選，讓按鈕有呼吸效果）
     */
    public void update(float deltaTime) {
        animationTime += deltaTime;
    }

    /**
     * 繪製開始畫面
     */
    public void draw(Canvas canvas) {
        if (isStarted) return;
        if (canvas == null) return;

        // 繪製背景
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);

        // 繪製遊戲標題（兩行）
        float titleY = screenHeight / 2f - 200;

        // 先畫描邊
        canvas.drawText("Cookie", screenWidth / 2f, titleY - 80, titleShadowPaint);
        canvas.drawText("Run", screenWidth / 2f, titleY + 80, titleShadowPaint);

        // 再畫主體
        canvas.drawText("Cookie", screenWidth / 2f, titleY - 80, titlePaint);
        canvas.drawText("Run", screenWidth / 2f, titleY + 80, titlePaint);

        // 計算按鈕呼吸動畫（輕微縮放）
        float breathScale = 1.0f + 0.05f * (float) Math.sin(animationTime * 3);

        canvas.save();
        canvas.scale(breathScale, breathScale, startButton.centerX(), startButton.centerY());

        // 繪製開始按鈕
        canvas.drawRoundRect(startButton, 40, 40, buttonPaint);
        canvas.drawRoundRect(startButton, 40, 40, buttonBorderPaint);

        // 繪製按鈕文字
        canvas.drawText("開始遊戲",
                startButton.centerX(),
                startButton.centerY() + 30,
                buttonTextPaint);

        canvas.restore();

        // 繪製提示文字
        canvas.drawText("點擊按鈕開始",
                screenWidth / 2f,
                screenHeight - 150,
                hintPaint);
    }

    /**
     * 處理觸控事件
     * @return true 如果點擊了開始按鈕
     */
    public boolean onTouchEvent(float touchX, float touchY) {
        if (isStarted) return false;

        if (startButton.contains(touchX, touchY)) {
            isStarted = true;
            return true;
        }
        return false;
    }

    /**
     * 回傳遊戲是否已開始
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * 重置開始畫面（重玩時使用）
     */
    public void reset() {
        isStarted = false;
        animationTime = 0;
    }
}
