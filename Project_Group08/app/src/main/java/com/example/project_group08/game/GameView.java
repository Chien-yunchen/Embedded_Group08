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
import com.example.project_group08.world.Ground;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private HpBar hpBar;
    private GameOverUI gameOverUI;
    private StartMenuUI startMenuUI;

    private Player player;
    private Ground ground;

    private Paint playerPaint;

    private long lastUpdateTime = 0;
    private float gameTime = 0;

    // ⭐ 紀錄這一局有沒有掉進洞洞
    private boolean hasFallen = false;

    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        getHolder().addCallback(this);

        hpBar = new HpBar();
        startMenuUI = new StartMenuUI(1, 1);
        gameOverUI  = new GameOverUI(1, 1);

        playerPaint = new Paint();
        playerPaint.setColor(Color.WHITE);
        playerPaint.setStyle(Paint.Style.FILL);

        gameThread = new GameThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();

        startMenuUI = new StartMenuUI(width, height);
        gameOverUI  = new GameOverUI(width, height);

        // 先 new Ground，讓它算好 GROUND_COLLISION_Y
        ground = new Ground(getContext(), width, height);

        // Player 的腳底高度 = Ground 的碰撞高度
        float groundY = Ground.GROUND_COLLISION_Y;

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

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {}

    public void update() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (lastUpdateTime == 0) ? 0.016f : (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        // 還在開始畫面 → 只更新開始動畫
        if (!startMenuUI.isStarted()) {
            startMenuUI.update(deltaTime);
            return;
        }

        // 1️⃣ 不管有沒有 GameOver，地板一律更新（背景還是會動）
        if (ground != null) {
            ground.update();
        }

        // 2️⃣ 還沒 GameOver 的情況下，才判斷是否踩到洞洞 & HP 歸零
        if (!gameOverUI.getIsGameOver() && ground != null && player != null) {

            if (!hasFallen) {
                // 只在「還沒掉進洞」時檢查一次
                boolean isOnHole = ground.isPlayerFalling(
                        (int) player.getX(),   // 角色固定 X
                        (int) player.getY()    // 腳底 Y
                );

                if (isOnHole) {
                    // 第一次掉進洞
                    hasFallen = true;
                    player.setIgnoreGroundCollision(true); // 後續不再被地板接住
                    gameOverUI.setGameOver(true);          // 立刻顯示遊戲結束畫面
                } else {
                    // 正常跑在地板上
                    player.setIgnoreGroundCollision(false);
                }
            }
        }

        // 如果已經掉下去了 → 強制一直忽略地板，繼續往下掉
        if (hasFallen && player != null) {
            player.setIgnoreGroundCollision(true);
        }

        // 3️⃣ Player 一律更新（即使 GameOver 了，掉洞洞時還是會繼續掉）
        if (player != null && !player.isGameOver()) {
            player.update();
        }

        // 4️⃣ 掉進洞且掉到螢幕下方一段距離 → 把角色凍住（此時畫面上看不到他）
        if (player != null && hasFallen && !player.isGameOver()) {
            float screenHeight = getHeight();
            if (player.getY() > screenHeight + 100) {
                player.setGameOver(true);
            }
        }

        // 5️⃣ HP / 存活時間只在未 GameOver 時更新
        if (!gameOverUI.getIsGameOver()) {
            gameTime += deltaTime;
            hpBar.update(deltaTime);
            gameOverUI.updateSurvivalTime(deltaTime);

            // HP 歸零的 Game Over：只在「還沒掉洞洞」時才會生效
            if (!hasFallen && hpBar.isGameOver()) {
                gameOverUI.setGameOver(true);
                if (player != null) player.setGameOver(true);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas == null) return;
        super.draw(canvas);

        if (!startMenuUI.isStarted()) {
            startMenuUI.draw(canvas);
            return;
        }

        canvas.drawColor(Color.BLACK);

        if (ground != null) ground.draw(canvas);

        // ⭐ Player 會自己掉出螢幕；掉出畫面後因 Y > 螢幕高度，就自然看不到
        if (player != null) player.draw(canvas, playerPaint);

        hpBar.draw(canvas);
        gameOverUI.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // 1️⃣ 還在開始畫面 → 處理開始按鈕
            if (!startMenuUI.isStarted()) {
                if (startMenuUI.onTouchEvent(x, y)) {
                    lastUpdateTime = 0;
                }
                return true;
            }

            // 2️⃣ Game Over 畫面 → 處理重新開始（要按到按鈕）
            if (gameOverUI.getIsGameOver()) {
                if (gameOverUI.onTouchEvent(x, y)) {
                    restartGame();
                }
                return true;
            }

            // 3️⃣ 遊戲進行中 → 點一下就跳
            if (player != null) {
                player.jump();
            }
            return true;
        }

        return false;
    }

    private void restartGame() {
        hpBar.reset();
        gameOverUI.reset();
        lastUpdateTime = 0;
        gameTime = 0;
        hasFallen = false;      // 新的一局還沒掉洞

        int w = getWidth();
        int h = getHeight();

        ground = new Ground(getContext(), w, h);
        float groundY = Ground.GROUND_COLLISION_Y + 40; // 覺得太低可以改回不要 +40
        player = new Player(w, groundY);
        player.setAnimations(
                AnimationFactory.createRunAnimation(getContext()),
                AnimationFactory.createJumpAnimation(getContext())
        );
    }
}
