package com.example.project_group08.player;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 負責「角色本體＋跳躍邏輯＋動畫顯示」的類別
 * - x：固定在畫面 1/4 位置
 * - y：由重力控制，只管上下（y 代表腳底）
 */
public class Player {

    // 固定 X 位置
    private final float x;

    // 垂直位置與速度（y 代表腳底）
    private float y;
    private float velocityY;

    // 狀態
    private boolean isJumping = false;
    private boolean isGameOver = false;

    // ⭐ 踩到洞洞後，忽略地板碰撞，讓角色自由落體
    private boolean ignoreGroundCollision = false;

    // 地板高度（腳底的 Y），由外部提供
    private float groundY;

    // 角色寬高（之後會依據動畫圖片調整）
    private float width;
    private float height;

    // 物理參數（之後可以自己微調）
    private static final float GRAVITY = 1.0f;       // 重力加速度
    private static final float JUMP_VELOCITY = -25f; // 起跳初速（負值往上）

    // ====== 動畫相關 ======
    private Animation runAnim;
    private Animation jumpAnim;
    private Sprite sprite;          // 負責真正畫出 bitmap

    public Player(float screenWidth, float groundY) {
        // 固定在畫面 1/4
        this.x = screenWidth * 0.25f;

        this.groundY = groundY;
        this.y = groundY;

        this.velocityY = 0f;

        // 先給一個預設大小（如果沒設定動畫時用）
        this.width = 80f;
        this.height = 120f;
    }

    /**
     * 由外部設定跑步 / 跳躍動畫
     */
    public void setAnimations(Animation runAnim, Animation jumpAnim) {
        this.runAnim = runAnim;
        this.jumpAnim = jumpAnim;

        // 預設先用跑步動畫
        if (runAnim != null) {
            this.sprite = new Sprite(runAnim);

            // 根據第一張圖來調整寬高，讓碰撞盒跟圖片一致
            Bitmap frame = runAnim.getFrame();
            if (frame != null) {
                this.width = frame.getWidth() * 0.7f;
                this.height = frame.getHeight() * 0.05f;  // 保留你原本的設定
            }
        }
    }

    /** 讓外部更新地板高度（例如場景改變） */
    public void setGroundY(float groundY) {
        this.groundY = groundY;
        if (y > groundY) {
            y = groundY;
            velocityY = 0;
            isJumping = false;
        }
    }

    /** 讓遊戲結束時把角色凍住 */
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    /** 踩到洞洞時，忽略地板碰撞，開始往下掉 */
    public void setIgnoreGroundCollision(boolean ignore) {
        this.ignoreGroundCollision = ignore;

        // 只要在洞洞上，就算是在空中 → 用跳躍 / 掉落動畫
        if (ignore) {
            this.isJumping = true;
        }
    }

    /** 對外的跳躍接口：不在空中、沒 GameOver 才能跳 */
    public void jump() {
        if (!isJumping && !isGameOver) {
            velocityY = JUMP_VELOCITY;
            isJumping = true;
        }
    }

    /** 內部用：套用重力，更新 y 位置 */
    private void applyGravity() {
        velocityY += GRAVITY;
        y += velocityY;

        // ⭐ 只有「沒有踩到洞洞」時才會被地板接住
        if (!ignoreGroundCollision && y >= groundY) {
            y = groundY;
            velocityY = 0;
            isJumping = false;
        }
        // 如果 ignoreGroundCollision == true → 不進來這個 if，
        // 角色就會一路往下掉，直到 GameView 判定 Game Over。
    }

    /** 每一幀更新角色邏輯（垂直運動＋動畫） */
    public void update() {
        if (isGameOver) {
            return; // 遊戲結束就不動
        }

        // 物理
        applyGravity();

        // 動畫更新
        if (sprite != null) {
            // 依照是否在空中切換動畫
            if (isJumping && jumpAnim != null) {
                sprite.setAnimation(jumpAnim);
            } else if (runAnim != null) {
                sprite.setAnimation(runAnim);
            }

            sprite.update();
        }
    }

    /**
     * 畫出角色：
     * - 如果有動畫，就畫 sprite（動畫）
     * - 如果還沒設定動畫，就退回畫矩形
     */
    public void draw(Canvas canvas, Paint paint) {
        // 計算角色左上角位置（因為 y 是「腳底」、x 是「中心」）
        float left = x - width / 2f;
        float top = y - height;
        float right = left + width;
        float bottom = top + height;

        if (sprite != null) {
            // 使用動畫貼圖
            sprite.draw(canvas, left, top);

            // debug 要看碰撞框可以打開這段：
            // if (paint != null) {
            //     canvas.drawRect(new RectF(left, top, right, bottom), paint);
            // }
        } else {
            // 沒有設定動畫時，用原本的矩形方式顯示
            if (paint != null) {
                canvas.drawRect(new RectF(left, top, right, bottom), paint);
            }
        }
    }

    // ====== 一些 getter，給其他系統（例如碰撞判斷）用 ======
    public float getX() {
        return x;
    }

    /** 回傳腳底的 Y（給 Ground.isPlayerFalling 用） */
    public float getY() {
        return y;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    // ⭐ 新增：回傳角色的碰撞框（給 Candy 做碰撞判斷）
    public Rect getCollisionRect() {
        float left = x - width / 2f;
        float top = y - height;
        float right = left + width;
        float bottom = top + height;

        // 也可以視覺上縮小一點碰撞框，例如 *0.8f，有需要再調
        return new Rect(
                (int) left,
                (int) top,
                (int) right,
                (int) bottom
        );
    }
}
