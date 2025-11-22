package com.example.project_group08.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;

public class GameOverUI {
    private boolean isGameOver = false;
    private int survivalTime = 0;

    private float gameOverShowTime = 0;  // GAME OVER 顯示時間計時器
    private final float GAME_OVER_DURATION = 3f;  // 停留 3 秒

    private Paint darkPaint;
    private Paint gameOverPaint;
    private Paint timePaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    private Paint buttonBorderPaint;

    private RectF startButton;

    private int screenWidth = 800;
    private int screenHeight = 1200;

    // 回調接口 - 用來通知需要重新開始
    private GameOverListener gameOverListener;

    public interface GameOverListener {
        void onGameOverTimeout();
    }

    public GameOverUI(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // 半透明背景
        darkPaint = new Paint();
        darkPaint.setColor(Color.argb(180, 0, 0, 0));
        darkPaint.setStyle(Paint.Style.FILL);

        // GAME OVER 大文字（紅色，帶描邊效果）
        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(120);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);
        gameOverPaint.setStyle(Paint.Style.FILL);
        gameOverPaint.setShadowLayer(10, 5, 5, Color.BLACK);  // 陰影效果

        // 時間文字
        timePaint = new Paint();
        timePaint.setColor(Color.WHITE);
        timePaint.setTextSize(50);
        timePaint.setTextAlign(Paint.Align.CENTER);

        // START 按鈕（漸變綠色）
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.rgb(76, 175, 80));  // 更鮮豔的綠色
        buttonPaint.setStyle(Paint.Style.FILL);

        // 按鈕邊框（黃色描邊）
        buttonBorderPaint = new Paint();
        buttonBorderPaint.setColor(Color.YELLOW);
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(8);

        // 按鈕文字（大且粗）
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(80);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        buttonTextPaint.setShadowLayer(5, 2, 2, Color.BLACK);

        // START 按鈕位置（正中間，更大）
        float buttonCenterX = screenWidth / 2f;
        float buttonCenterY = screenHeight / 2f + 150;
        float buttonWidth = 400;   // 更大
        float buttonHeight = 150;  // 更大

        startButton = new RectF(
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
            gameOverShowTime = 0;  // 重置計時器
        }
    }

    /**
     * 更新存活時間
     */
    public void updateSurvivalTime(float deltaTime) {
        survivalTime += (int)deltaTime;
    }

    /**
     * 更新計時器（在 GameView.update() 中調用）
     */
    public void update(float deltaTime) {
        if (isGameOver) {
            gameOverShowTime += deltaTime;

            // 如果超過 3 秒，自動重新開始
            if (gameOverShowTime >= GAME_OVER_DURATION) {
                if (gameOverListener != null) {
                    gameOverListener.onGameOverTimeout();
                }
            }
        }
    }

    /**
     * 繪製 Game Over 介面
     */
    /**
     * 繪製 Game Over 介面
     */
    public void draw(Canvas canvas) {
        if (!isGameOver) return;
        if (canvas == null) return;

        // 繪製半透明背景
        canvas.drawRect(0, 0, screenWidth, screenHeight, darkPaint);

        // 計算 GAME OVER 的縮放動畫（0-1 秒內從小到大）
        float scaleProgress = Math.min(gameOverShowTime / 0.5f, 1f);  // 0.5 秒內完成放大
        float scale = 0.3f + (scaleProgress * 0.7f);  // 從 0.3 倍放大到 1.0 倍

        // 保存 canvas 狀態
        canvas.save();

        // 應用縮放變換（以正中間為中心）
        canvas.scale(scale, scale, screenWidth / 2f, screenHeight / 2f - 150);

        // 繪製「GAME OVER」大文字（正中間，紅色，大大的）
        canvas.drawText("GAME OVER",
                screenWidth / 2f,
                screenHeight / 2f - 150,
                gameOverPaint);

        // 恢復 canvas 狀態
        canvas.restore();

        // 繪製存活時間
        canvas.drawText("Survival Time: " + survivalTime + "s",
                screenWidth / 2f,
                screenHeight / 2f - 30,
                timePaint);

        // 計算按鈕是否應該顯示（3秒內顯示）
        if (gameOverShowTime < GAME_OVER_DURATION) {
            // 繪製 START 按鈕（圓角 + 邊框）
            canvas.drawRoundRect(startButton, 30, 30, buttonPaint);
            canvas.drawRoundRect(startButton, 30, 30, buttonBorderPaint);

            // 按鈕文字
            canvas.drawText("START",
                    startButton.centerX(),
                    startButton.centerY() + 30,
                    buttonTextPaint);
        } else {
            // 3秒後顯示「自動重新開始...」
            Paint autoPaint = new Paint();
            autoPaint.setColor(Color.YELLOW);
            autoPaint.setTextSize(40);
            autoPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("自動重新開始...",
                    screenWidth / 2f,
                    screenHeight / 2f + 150,
                    autoPaint);
        }
    }

    /**
     * 檢查是否點擊了 START 按鈕
     */
    public boolean onTouchEvent(float touchX, float touchY) {
        return startButton.contains(touchX, touchY);
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

    // 設置回調監聽
    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public float getGameOverShowTime() {
        return gameOverShowTime;
    }
}
