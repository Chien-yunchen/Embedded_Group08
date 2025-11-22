package com.example.project_group08.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class HpBar {
    private float maxHP = 100;
    private float currentHP = 100;
    private float hpDecayPerSecond = 2.5f;  // 每秒扣 2.5 HP

    private float x = 20;
    private float y = 30;
    private float barWidth = 400;  // 改大一倍
    private float barHeight = 40;  // 改大一倍

    private Paint redPaint;
    private Paint greenPaint;
    private Paint blackPaint;
    private Paint whitePaint;

    public HpBar() {
        // 初始化繪圖工具
        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.FILL);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.FILL);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeWidth(2);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(24);
    }

    /**
     * 更新血量（根據時間扣血）
     * @param deltaTime 自上次更新經過的時間（秒）
     */
    public void update(float deltaTime) {
        currentHP -= hpDecayPerSecond * deltaTime;

        // 血量不能低於 0
        if (currentHP < 0) {
            currentHP = 0;
        }
    }

    /**
     * 繪製血條
     */
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 計算血條實際寬度（依照 HP 比例）
        float hpRatio = currentHP / maxHP;
        float currentBarWidth = barWidth * hpRatio;

        // 背景黑色框
        canvas.drawRect(x, y, x + barWidth, y + barHeight, blackPaint);

        // 血條顏色根據 HP 變化
        Paint hpColor = (hpRatio > 0.5f) ? greenPaint : redPaint;

        // 繪製血條（綠→紅漸變）
        canvas.drawRect(x, y, x + currentBarWidth, y + barHeight, hpColor);

        // 顯示 HP 數字
        canvas.drawText("HP: " + (int)currentHP + "/" + (int)maxHP,
                x + barWidth + 20, y + barHeight, whitePaint);
    }

    /**
     * 檢查是否遊戲結束（血量用盡）
     */
    public boolean isGameOver() {
        return currentHP <= 0;
    }

    /**
     * 重置血條
     */
    public void reset() {
        currentHP = maxHP;
    }

    // Getter 方法
    public float getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(float hp) {
        this.currentHP = hp;
    }

    public void setHpDecayPerSecond(float decay) {
        this.hpDecayPerSecond = decay;
    }
}
