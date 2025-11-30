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

        // ★ 一定要先 new Ground，讓它算好 GROUND_COLLISION_Y
        ground = new Ground(getContext(), width, height);

        // ★ Player 的腳底高度 = Ground 的碰撞高度
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

        if (!startMenuUI.isStarted()) {
            startMenuUI.update(deltaTime);
            return;
        }

        if (!gameOverUI.getIsGameOver()) {

            if (ground != null) ground.update();
            if (player != null) player.update();

            if (ground != null && player != null) {
                if (ground.isPlayerFalling((int) player.getX(), (int) player.getY())) {
                    gameOverUI.setGameOver(true);
                    player.setGameOver(true);
                }
            }

            gameTime += deltaTime;
            hpBar.update(deltaTime);
            gameOverUI.updateSurvivalTime(deltaTime);

            if (hpBar.isGameOver()) {
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
        if (player != null) player.draw(canvas, playerPaint);
        hpBar.draw(canvas);
        gameOverUI.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (!startMenuUI.isStarted()) {
                if (startMenuUI.onTouchEvent(x, y)) {
                    lastUpdateTime = 0;
                }
                return true;
            }

            if (gameOverUI.getIsGameOver()) {
                if (gameOverUI.onTouchEvent(x, y)) {
                    restartGame();
                }
                return true;
            }

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

        int w = getWidth();
        int h = getHeight();

        ground = new Ground(getContext(), w, h);
        float groundY = Ground.GROUND_COLLISION_Y;

        player = new Player(w, groundY);
        player.setAnimations(
                AnimationFactory.createRunAnimation(getContext()),
                AnimationFactory.createJumpAnimation(getContext())
        );
    }
}
