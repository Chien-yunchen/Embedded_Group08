package com.example.project_group08.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;

public class GameOverUI {
    private boolean isGameOver = false;
    private int survivalTime = 0;

    private float gameOverShowTime = 0;

    private Paint darkPaint;
    private Paint gameOverPaint;
    private Paint timePaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    private Paint buttonBorderPaint;

    private RectF restartButton;

    private int screenWidth = 800;
    private int screenHeight = 1200;

    public GameOverUI(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // 半透明背景
        darkPaint = new Paint();
        darkPaint.setColor(Color.argb(180, 0, 0, 0));
        darkPaint.setStyle(Paint.Style.FILL);

        // 遊戲結束文字（紅色，非常大）
        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(240);  // 改到 240，非常大
        gameOverPaint.setTextAlign(Paint.Align.CENTER);
        gameOverPaint.setStyle(Paint.Style.FILL);
        gameOverPaint.setShadowLayer(20, 5, 5, Color.BLACK);  // 更大的陰影
        gameOverPaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        gameOverPaint.setStrokeWidth(4);  // 更厚的筆畫

        // 時間文字
        timePaint = new Paint();
        timePaint.setColor(Color.WHITE);
        timePaint.setTextSize(60);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        // 重新開始按鈕（綠色）
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.rgb(76, 175, 80));
        buttonPaint.setStyle(Paint.Style.FILL);

        // 按鈕邊框（黃色描邊）
        buttonBorderPaint = new Paint();
        buttonBorderPaint.setColor(Color.YELLOW);
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(8);

        // 按鈕文字
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(80);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        buttonTextPaint.setShadowLayer(5, 2, 2, Color.BLACK);

        // 重新開始按鈕位置 - 更靠下，與生存時間分開更遠
        float buttonCenterX = screenWidth / 2f;
        float buttonCenterY = screenHeight / 2f + 280;  // 從 180 改到 280，更靠下
        float buttonWidth = 320;
        float buttonHeight = 140;

        restartButton = new RectF(
                buttonCenterX - buttonWidth / 2,
                buttonCenterY - buttonHeight / 2,
                buttonCenterX + buttonWidth / 2,
                buttonCenterY + buttonHeight / 2
        );
    }

    /**
     * 設置遊戲結束
     */
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        if (gameOver) {
            gameOverShowTime = 0;
        }
    }

    /**
     * 更新存活時間
     */
    public void updateSurvivalTime(float deltaTime) {
        survivalTime += (int)deltaTime;
    }

    /**
     * 繪製遊戲結束介面
     */
    public void draw(Canvas canvas) {
        if (!isGameOver) return;
        if (canvas == null) return;

        // 繪製半透明背景
        canvas.drawRect(0, 0, screenWidth, screenHeight, darkPaint);

        // 計算縮放動畫
        float scaleProgress = Math.min(gameOverShowTime / 0.5f, 1f);
        float scale = 0.3f + (scaleProgress * 0.7f);

        canvas.save();
        canvas.scale(scale, scale, screenWidth / 2f, screenHeight / 2f - 150);

        // 繪製「遊戲結束」文字 - 超大
        canvas.drawText("遊戲結束",
                screenWidth / 2f,
                screenHeight / 2f - 150,
                gameOverPaint);

        canvas.restore();

        // 繪製存活時間
        canvas.drawText("生存時間: " + survivalTime + " 秒",
                screenWidth / 2f,
                screenHeight / 2f + 80,
                timePaint);

        // 繪製重新開始按鈕 - 與生存時間分開更遠
        canvas.drawRoundRect(restartButton, 40, 40, buttonPaint);
        canvas.drawRoundRect(restartButton, 40, 40, buttonBorderPaint);

        canvas.drawText("重新開始",
                restartButton.centerX(),
                restartButton.centerY() + 30,
                buttonTextPaint);
    }

    /**
     * 檢查是否點擊了重新開始按鈕
     */
    public boolean onTouchEvent(float touchX, float touchY) {
        return restartButton.contains(touchX, touchY);
    }

    /**
     * 重置遊戲結束介面
     */
    public void reset() {
        isGameOver = false;
        survivalTime = 0;
        gameOverShowTime = 0;
    }

    // Getter 方法
    public boolean getIsGameOver() {
        return isGameOver;
    }

    public int getSurvivalTime() {
        return survivalTime;
    }
}