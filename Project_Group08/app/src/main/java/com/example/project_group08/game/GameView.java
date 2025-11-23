package com.example.project_group08.game;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 遊戲主視圖
 * 負責協調所有遊戲元素的更新和繪製
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private HpBar hpBar;
    private GameOverUI gameOverUI;

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

        // 初始化 UI 元件
        hpBar = new HpBar();
        gameOverUI = new GameOverUI(getWidth(), getHeight());

        gameThread = new GameThread(getHolder(), this);
    }

    /**
     * Surface 建立時開始遊戲迴圈
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 重新初始化 GameOverUI（確保寬高正確）
        gameOverUI = new GameOverUI(getWidth(), getHeight());

        if (gameThread.getState() == Thread.State.NEW) {
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

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
        float deltaTime = lastUpdateTime == 0 ? 0.016f : (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        // 如果遊戲未結束，更新遊戲時間和血條
        if (!gameOverUI.getIsGameOver()) {
            gameTime += deltaTime;

            // 更新血條（每幀減少 HP）
            hpBar.update(deltaTime);

            // 更新存活時間
            gameOverUI.updateSurvivalTime(deltaTime);

            // 檢查遊戲是否結束
            if (hpBar.isGameOver()) {
                gameOverUI.setGameOver(true);
            }
        }
    }

    /**
     * 遊戲繪製
     */
    public void draw(Canvas canvas) {
        if (canvas == null) return;
        super.draw(canvas);
        // 清空畫布（黑色背景）
        canvas.drawColor(android.graphics.Color.BLACK);

        // 繪製遊戲元素（由其他組員添加）
        // 例：背景、地板、角色 等

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
                    // 遊戲進行中，角色跳躍事件（由組員 A 處理）
                    // TODO: playerJump();
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