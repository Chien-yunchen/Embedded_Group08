package com.example.project_group08.game;

import com.example.project_group08.ui.HpBar;
import com.example.project_group08.ui.GameOverUI;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.project_group08.player.Player;
import com.example.project_group08.player.AnimationFactory;   // ★ 新增：匯入動畫工廠

/**
 * 遊戲主視圖
 * 負責協調所有遊戲元素的更新和繪製
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private HpBar hpBar;
    private GameOverUI gameOverUI;

    // A：新增的角色與地板參數
    private Player player;
    private Paint playerPaint;
    private float groundY;           // 地板高度（腳站的位置）

    private long lastUpdateTime = 0;
    private float gameTime = 0;  // 遊戲累計時間（秒）

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);

        // 初始化 UI 元件（先用預設，真正尺寸在 surfaceCreated 再更新一次）
        hpBar = new HpBar();
        gameOverUI = new GameOverUI(getWidth(), getHeight());

        // 初始化畫 Player 用的畫筆（現在主要用來畫 debug 或碰撞框）
        playerPaint = new Paint();
        playerPaint.setColor(Color.WHITE);
        playerPaint.setStyle(Paint.Style.FILL);

        gameThread = new GameThread(getHolder(), this);
    }

    /**
     * Surface 建立時開始遊戲迴圈
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();

        // 重新初始化 GameOverUI（確保寬高正確）
        gameOverUI = new GameOverUI(width, height);

        // 設定地板高度（暫時用畫面高度的 3/4，之後 B 可以改）
        groundY = height * 0.75f;

        // 建立 Player（X 固定在畫面 1/4，由 Player 裡面自己處理）
        player = new Player(width, groundY);

        // ★ 在這裡幫 Player 設定跑步與跳躍動畫
        player.setAnimations(
                AnimationFactory.createRunAnimation(getContext()),
                AnimationFactory.createJumpAnimation(getContext())
        );

        if (gameThread.getState() == Thread.State.NEW) {
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 遊戲邏輯更新
     */
    public void update() {
        // 計算 deltaTime（每幀時間間隔）
        long currentTime = System.currentTimeMillis();
        float deltaTime = (lastUpdateTime == 0) ? 0.016f : (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        // 遊戲未結束才更新
        if (!gameOverUI.getIsGameOver()) {

            // 更新玩家（含跳躍重力 + 動畫）
            if (player != null) {
                player.update();
            }

            // 更新遊戲時間
            gameTime += deltaTime;

            // 更新血條（每幀減少 HP）
            hpBar.update(deltaTime);

            // 更新存活時間顯示
            gameOverUI.updateSurvivalTime(deltaTime);

            // 檢查遊戲是否結束
            if (hpBar.isGameOver()) {
                gameOverUI.setGameOver(true);
                if (player != null) {
                    player.setGameOver(true);   // 把角色也鎖住
                }
            }
        }
    }

    /**
     * 遊戲繪製
     */
    @Override
    public void draw(Canvas canvas) {
        if (canvas == null) return;
        super.draw(canvas);

        // 清空畫布（黑色背景）
        canvas.drawColor(Color.BLACK);

        // A：畫地板（簡單一條線）
        if (groundY > 0) {
            Paint groundPaint = new Paint();
            groundPaint.setColor(Color.GRAY);
            groundPaint.setStrokeWidth(8f);
            canvas.drawLine(0, groundY, canvas.getWidth(), groundY, groundPaint);
        }

        // A：畫角色
        // 現在 Player.draw() 會優先畫動畫，有動畫才用 Sprite，沒有才退回畫矩形
        if (player != null) {
            player.draw(canvas, playerPaint);
        }

        // 繪製血條
        hpBar.draw(canvas);

        // 最後繪製遊戲結束畫面（這樣才能顯示在最上層）
        gameOverUI.draw(canvas);
    }

    /**
     * 處理觸摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 如果遊戲結束，檢查是否點擊了重新開始按鈕
                if (gameOverUI.getIsGameOver()) {
                    if (gameOverUI.onTouchEvent(touchX, touchY)) {
                        restartGame();  // 按下按鈕重新開始遊戲
                        return true;
                    }
                } else {
                    // 遊戲進行中，角色跳躍事件
                    if (player != null) {
                        player.jump();
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    /**
     * 重新開始遊戲
     */
    private void restartGame() {
        hpBar.reset();
        gameOverUI.reset();
        gameTime = 0;
        lastUpdateTime = 0;

        if (player != null) {
            player.setGameOver(false);
            player.setGroundY(groundY); // 確保地板高度正確
            // 如果你希望重開遊戲時動畫回到跑步初始，也可以在 Player 裡多寫一個 reset()
        }

        // TODO: 重置其他遊戲元素（角色、地板、障礙等）
    }

    // Getter 方法
    public HpBar getHpBar() {
        return hpBar;
    }

    public GameOverUI getGameOverUI() {
        return gameOverUI;
    }

    public float getGameTime() {
        return gameTime;
    }
}
