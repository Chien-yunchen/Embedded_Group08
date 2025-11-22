package com.example.project_group08;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * 遊戲執行緒
 * 處理遊戲的更新和繪製迴圈
 */
public class GameThread extends Thread {

    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean isRunning = false;

    private final int FPS = 60;  // 每秒幀數
    private final long FRAME_TIME = 1000 / FPS;  // 每幀時間（毫秒）

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.isRunning = true;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (isRunning) {
            Canvas canvas = null;

            try {
                // 取得 Canvas 進行繪製
                canvas = surfaceHolder.lockCanvas(null);

                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        // 更新遊戲邏輯
                        gameView.update();

                        // 繪製遊戲
                        gameView.draw(canvas);
                    }
                }

                // 控制幀率
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - lastTime;

                if (elapsed < FRAME_TIME) {
                    Thread.sleep(FRAME_TIME - elapsed);
                }

                lastTime = System.currentTimeMillis();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }
}
