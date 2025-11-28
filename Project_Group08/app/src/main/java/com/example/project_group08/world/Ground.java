package com.example.project_group08.world;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.content.Context;
import android.graphics.BitmapFactory;
import java.util.LinkedList;
import java.util.Random;
import android.util.Log;
import com.example.project_group08.R;
import com.example.project_group08.world.Gap; // 確保 Gap 類別已被正確引入

/**
 * 組員 B 任務：處理地板滾動、圖塊生成以及作為掉落判定的主要接口。
 */
public class Ground {

    // --- B 必須提供的常數：給 A 組員使用 ---
    public static final int GROUND_TOP_POSITION = 240; // 地板頂部的Y座標 (像素值，需調整)

    // --- 遊戲設定 ---
    private static final int SCROLL_SPEED = 10; // 地板水平滾動速度 (像素/幀)
    private static final int TILE_WIDTH = 1024;  // 單一地板圖塊的寬度
    private static final int GROUND_HEIGHT = 747; // 地板的高度

    private static final int GAP_PERCENT_CHANCE = 70; // 洞洞生成的機率 (0 到 100)

    // --- 素材資源 ---
    private Bitmap floorBitmap;     // 完整的地板圖 (floor.jpg)
    private Bitmap gapBitmap;       // 帶有洞的圖 (floor w hole.jpg)
    private Bitmap skyBitmap;       // 天空背景圖 (sky.jpg)

    // --- 滾動狀態 ---
    private LinkedList<GroundTile> tiles; // 存放目前畫面上的地板區塊
    private int screenWidth; // 螢幕寬度
    private Random random;

    // 定義一個內部類別來表示一個地板區塊
    private class GroundTile {
        Bitmap bitmap;  // 存放圖片素材 (floorBitmap 或 gapBitmap)
        int x;          // 該圖塊在螢幕上的起始 X 座標

        // 構造函式：僅需要圖片和起始 X 座標
        public GroundTile(Bitmap bm, int startX) {
            this.bitmap = bm;
            this.x = startX;
        }
    }

    public Ground(Context context, int screenW, int screenH) {
        this.screenWidth = screenW;
        this.random = new Random();
        this.tiles = new LinkedList<>();

        floorBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        gapBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        skyBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // 預先填滿螢幕
        int currentX = 0;
        while (currentX < screenWidth + TILE_WIDTH) {
            // 使用新的 GroundTile 構造函式
            tiles.add(new GroundTile(floorBitmap, currentX));
            currentX += TILE_WIDTH;
        }
    }

    // 核心邏輯：移動地板並處理生成/移除
    public void update() {
        // 1. 移動所有地板區塊
        for (GroundTile tile : tiles) {
            tile.x -= SCROLL_SPEED;
        }

        // 2. 移除滾出螢幕的區塊 (從最左邊移除)
        // 檢查 tile.bitmap 是否為 null 並判斷是否滾出畫面
        if (!tiles.isEmpty() && tiles.getFirst().bitmap != null && tiles.getFirst().x + TILE_WIDTH < 0) {
            tiles.removeFirst();
        }

        // 3. 生成新的地板區塊 (補到最右邊)
        if (tiles.getLast().x + TILE_WIDTH < screenWidth) {
            generateNextTile();
        }
    }

    // 繪製邏輯：先畫天空，再畫地板
    public void draw(Canvas canvas) {
        // 1. 繪製固定不動的天空背景 (sky.jpg)
        Rect skyRect = new Rect(0, 0, screenWidth, GROUND_TOP_POSITION);
        canvas.drawBitmap(skyBitmap, null, skyRect, null);

        // 2. 繪製滾動中的地板 (floor.jpg / floor w hole.jpg)
        for (GroundTile tile : tiles) {
            // 確保只繪製在螢幕內且 bitmap 不為 null 的部分
            if (tile.bitmap != null && tile.x < screenWidth && tile.x + TILE_WIDTH > 0) {
                Rect destRect = new Rect(
                        tile.x,
                        GROUND_TOP_POSITION,
                        tile.x + TILE_WIDTH,
                        GROUND_TOP_POSITION + GROUND_HEIGHT
                );
                canvas.drawBitmap(tile.bitmap, null, destRect, null);
            }
        }
    }

    // 隨機生成下一個區塊：普通地板或洞圖塊
    private void generateNextTile() {
        int lastX = tiles.getLast().x + TILE_WIDTH;

        // 使用 GAP_PERCENT_CHANCE 決定是否生成【有洞的圖片】
        if (random.nextInt(100) < GAP_PERCENT_CHANCE) {

            // 使用新的 GroundTile 構造函式
            tiles.add(new GroundTile(gapBitmap, lastX));

        } else {

            // 使用新的 GroundTile 構造函式
            tiles.add(new GroundTile(floorBitmap, lastX));
        }
    }

    /**
     * 供組員 A 判斷角色是否掉落的接口
     * 核心邏輯：將精準判定委託給 Gap.java 處理。
     */
    public boolean isPlayerFalling(int playerX, int playerY) {
        // 1. 檢查 Y 軸：角色是否已經落地
        if (playerY < GROUND_TOP_POSITION) {
            return false;
        }

        // 2. 檢查 X 軸：遍歷所有在畫面上的 GroundTile
        for (GroundTile tile : tiles) {

            // a. 檢查 PlayerX 是否落在這個圖塊的水平範圍內
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {

                // b. 將精準判定委託給 Gap.java 處理
                // 傳遞 tile.bitmap 和 tile.x 作為參數
                if (Gap.checkFalling(playerX, tile.bitmap, tile.x, gapBitmap, TILE_WIDTH)) {
                    // 角色落在了有洞圖塊的洞區間內，判定掉落
                    return true;
                }

                // 如果落在了任何圖塊的實體部分 (無論是 floorBitmap 還是 gapBitmap 的非洞區)，則安全
                return false;
            }
        }

        // 如果沒有落在任何圖塊上 (理論上在無限滾動中不會發生)
        return false;
    }

    /**
     * 除錯方法：將目前畫面上所有地板區塊的狀態輸出到 Logcat

    public void logTiles() {
        StringBuilder sb = new StringBuilder("Tiles: ");
        for (GroundTile tile : tiles) {
            // 判斷 tile.bitmap 是否為 gapBitmap
            String tileType = (tile.bitmap == gapBitmap) ? "(GAP_TILE)" : "(FLOOR_TILE)";

            sb.append("[x=").append(tile.x)
                    .append(", Type: ").append(tileType)
                    .append("] ");
        }
        Log.d("GROUND_DEBUG", sb.toString());
    }
     */
}