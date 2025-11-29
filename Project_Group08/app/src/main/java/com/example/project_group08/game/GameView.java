package com.example.project_group08.game;

import com.example.project_group08.ui.HpBar;
import com.example.project_group08.ui.GameOverUI;
import com.example.project_group08.ui.StartMenuUI;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.project_group08.player.Player;
import com.example.project_group08.player.AnimationFactory;

/**
 * 遊戲主視圖
 * 負責協調所有遊戲元素的更新和繪製
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private HpBar hpBar;
    private GameOverUI gameOverUI;
    private StartMenuUI startMenuUI;

    private Player player;
    private Paint playerPaint;
    private float groundY;

    private long lastUpdateTime = 0;
    private float gameTime = 0;

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

        hpBar = new HpBar();
        gameOverUI = new GameOverUI(getWidth(), getHeight());
        startMenuUI = new StartMenuUI(getWidth(), getHeight());

        playerPaint = new Paint();
        playerPaint.setColor(Color.WHITE);
        playerPaint.setStyle(Paint.Style.FILL);

        gameThread = new GameThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();

        gameOverUI = new GameOverUI(width, height);
        startMenuUI = new StartMenuUI(width, height);

        groundY = height * 0.75f;

        player = new Player(width, groundY);
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
        long currentTime = System.currentTimeMillis();
        float deltaTime = (lastUpdateTime == 0) ? 0.016f : (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        // 如果還沒開始，只更新開始選單動畫
        if (!startMenuUI.isStarted()) {
            startMenuUI.update(deltaTime);
            return;
        }

        // 遊戲未結束才更新
        if (!gameOverUI.getIsGameOver()) {

            if (player != null) {
                player.update();
            }

            gameTime += deltaTime;
            hpBar.update(deltaTime);
            gameOverUI.updateSurvivalTime(deltaTime);

            if (hpBar.isGameOver()) {
                gameOverUI.setGameOver(true);
                if (player != null) {
                    player.setGameOver(true);
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

        // 如果還沒開始，只畫開始選單
        if (!startMenuUI.isStarted()) {
            startMenuUI.draw(canvas);
            return;
        }

        // 清空畫布（黑色背景）
        canvas.drawColor(Color.BLACK);

        // 畫地板
        if (groundY > 0) {
            Paint groundPaint = new Paint();
            groundPaint.setColor(Color.GRAY);
            groundPaint.setStrokeWidth(8f);
            canvas.drawLine(0, groundY, canvas.getWidth(), groundY, groundPaint);
        }

        // 畫角色
        if (player != null) {
            player.draw(canvas, playerPaint);
        }

        // 繪製血條
        hpBar.draw(canvas);

        // 最後繪製遊戲結束畫面
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
                // 如果還沒開始，處理開始選單點擊
                if (!startMenuUI.isStarted()) {
                    if (startMenuUI.onTouchEvent(touchX, touchY)) {
                        lastUpdateTime = 0;
                        return true;
                    }
                    return true;
                }

                // 如果遊戲結束，檢查是否點擊了重新開始按鈕
                if (gameOverUI.getIsGameOver()) {
                    if (gameOverUI.onTouchEvent(touchX, touchY)) {
                        restartGame();
                        return true;
                    }
                } else {
                    // 遊戲進行中，角色跳躍
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
            player.setGroundY(groundY);
        }
    }

    // Getter 方法
    public HpBar getHpBar() {
        return hpBar;
    }

    public GameOverUI getGameOverUI() {
        return gameOverUI;
    }

    public StartMenuUI getStartMenuUI() {
        return startMenuUI;
    }

    public float getGameTime() {
        return gameTime;
    }
}
