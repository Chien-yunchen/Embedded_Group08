package com.example.project_group08.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class HpBar {
    private float maxHP = 100;
    private float currentHP = 100;
    private float hpDecayPerSecond = 2.5f;  // 每秒扣 2.5 HP

    private int candyCount = 0;  // 糖果計數
    private static final int CANDIES_FOR_HEALING = 50;  // 50 顆糖果回血
    private static final float HP_RECOVER_AMOUNT = 25f;  // 回復 25 HP

    private float x = 20;
    private float y = 30;
    private float barWidth = 400;  // 改大一倍
    private float barHeight = 40;  // 改大一倍

    private Paint redPaint;
    private Paint greenPaint;
    private Paint blackPaint;
    private Paint whitePaint;
    private Paint candyPaint;

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
        whitePaint.setTextSize(40);  // 改大到 40
        whitePaint.setTextAlign(Paint.Align.LEFT);

        candyPaint = new Paint();
        candyPaint.setColor(Color.YELLOW);
        candyPaint.setTextSize(40);
        candyPaint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * 更新血量(根據時間扣血)
     * @param deltaTime 自上次更新經過的時間(秒)
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

        // 計算血條實際寬度(依照 HP 比例)
        float hpRatio = currentHP / maxHP;
        float currentBarWidth = barWidth * hpRatio;

        // 背景黑色框
        canvas.drawRect(x, y, x + barWidth, y + barHeight, blackPaint);

        // 血條顏色根據 HP 變化
        Paint hpColor = (hpRatio > 0.5f) ? greenPaint : redPaint;

        // 繪製血條(綠→紅漸變)
        canvas.drawRect(x, y, x + currentBarWidth, y + barHeight, hpColor);

        // 顯示 HP 數字(文字更大)
        canvas.drawText("HP: " + (int)currentHP + "/" + (int)maxHP,
                x + barWidth + 30, y + barHeight - 5, whitePaint);

        // 顯示糖果數
        canvas.drawText("🍬: " + candyCount,
                x + barWidth + 30, y + barHeight + 50, candyPaint);
    }

    /**
     * 吃到糖果，增加糖果計數，累積到 50 顆時回血
     * @param count 本次增加的糖果數(通常是 1)
     */
    public void addCandyCount(int count) {
        candyCount += count;

        // 每 50 顆糖果回復一次血量
        if (candyCount >= CANDIES_FOR_HEALING) {
            candyCount -= CANDIES_FOR_HEALING;
            recoverHP(HP_RECOVER_AMOUNT);
        }
    }

    /**
     * 恢復血量
     * @param amount 恢復的血量
     */
    private void recoverHP(float amount) {
        currentHP += amount;

        // 血量不能超過最大值
        if (currentHP > maxHP) {
            currentHP = maxHP;
        }
    }

    /**
     * 檢查是否遊戲結束(血量用盡)
     */
    public boolean isGameOver() {
        return currentHP <= 0;
    }

    /**
     * 重置血條和糖果計數
     */
    public void reset() {
        currentHP = maxHP;
        candyCount = 0;
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

    public int getCandyCount() {
        return candyCount;
    }
}
