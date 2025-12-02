package com.example.project_group08.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import com.example.project_group08.R;
import com.example.project_group08.world.Gap; // 確保 Gap 類別已被正確引入

import java.util.LinkedList;
import java.util.Random;

public class Ground {

    // === 調整後的參數 ===
    private static final int TILE_WIDTH = 1024;    // 單一地板圖片寬
    private static final int SCROLL_SPEED = 10;
    private static final int GAP_PERCENT_CHANCE = 70; // 設為 70% 機率生成有洞圖塊

    // 🚀 新增：一般地板的額外縮放因子
    private static final float FLOOR_SCALE_FACTOR = 1.2f;

    public static int GROUND_TOP_POSITION;      // 地板開始 Y
    public static int GROUND_COLLISION_Y;       // 薑餅人腳底 Y（用於落地判斷）

    private Bitmap floorBitmap;
    private Bitmap gapBitmap;
    private Bitmap skyBitmap;

    // 新增：紀錄縮放後的 floorBitmap 高度
    private int scaledFloorHeight;

    private final int screenWidth;
    private final int screenHeight;

    private final LinkedList<GroundTile> tiles = new LinkedList<>();
    private final Random random = new Random();

    private final Rect destRect = new Rect();

    private class GroundTile {
        Bitmap bitmap;
        int x;
        GroundTile(Bitmap bm, int startX) {
            bitmap = bm;
            x = startX;
        }
    }

    public Ground(Context context, int screenW, int screenH) {
        screenWidth = screenW;
        screenHeight = screenH;

        // ⭐ 讓地板高度占畫面 40% (作為基礎高度)
        int groundHeight = (int)(screenH * 0.4f);

        // ⭐ 地板開始位置：從螢幕底部往上 groundHeight
        GROUND_TOP_POSITION = screenH - groundHeight;

        // ⭐ 薑餅人腳底碰撞高度（草皮上緣，適度往下）
        GROUND_COLLISION_Y = GROUND_TOP_POSITION + 180; // 保持相對位置

        // === 載入圖片 (已修正為 R.drawable 載入) ===
        Bitmap rawFloor = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        Bitmap rawGap   = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        Bitmap rawSky   = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // ⭐ 天空鋪滿整個畫布
        if (rawSky != null) {
            skyBitmap = Bitmap.createScaledBitmap(rawSky, screenW, screenH, true);
        } else {
            Log.e("Ground", "Sky bitmap failed to load. (sky.jpg)");
        }

        // ⭐ 地板依照固定高度縮放
        if (rawFloor != null) {
            // 🚀 關鍵修正 1: 計算額外放大後的 floorBitmap 高度
            scaledFloorHeight = (int)(groundHeight * FLOOR_SCALE_FACTOR);

            // 關鍵：floorBitmap 使用 scaledFloorHeight 進行縮放
            floorBitmap = Bitmap.createScaledBitmap(rawFloor, TILE_WIDTH, scaledFloorHeight, true);
        } else {
            Log.e("Ground", "Floor bitmap failed to load. (floor.jpg)");
        }

        if (rawGap != null) {
            // 關鍵：gapBitmap 使用基礎 groundHeight (未放大)
            gapBitmap   = Bitmap.createScaledBitmap(rawGap,   TILE_WIDTH, groundHeight, true);
        } else {
            Log.e("Ground", "Gap bitmap failed to load. (floor_w_hole.jpg)");
        }


        // === 初始化地板 tiles ===
        int x = 0;

        // 🚨 修正：確保第一個圖塊 (x=0) 永遠是普通地板 (FLOOR_TILE)
        tiles.add(new GroundTile(floorBitmap, x));
        x += TILE_WIDTH;

        // 確保後續圖塊填充直到填滿畫面
        while (x < screenW + TILE_WIDTH) {

            // 💡 填充後續圖塊時，也應該使用 floorBitmap
            tiles.add(new GroundTile(floorBitmap, x));
            x += TILE_WIDTH;
        }
    }

    public void update() {
        if (tiles.isEmpty()) return;

        // 移動
        for (GroundTile tile : tiles) {
            tile.x -= SCROLL_SPEED;
        }

        // 左邊移出畫面就刪除
        while (!tiles.isEmpty() && tiles.getFirst().x + TILE_WIDTH < 0) {
            tiles.removeFirst();
        }

        // 右邊補 tiles
        // 🚀 修正：應檢查最後一個圖塊是否滾入畫面即可生成下一個。
        while (!tiles.isEmpty() && tiles.getLast().x + TILE_WIDTH < screenWidth) {
            generateNextTile();
        }
    }

    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 1️⃣ 先畫天空（鋪滿整個畫面）
        if (skyBitmap != null) {
            canvas.drawBitmap(skyBitmap, null, new Rect(0, 0, screenWidth, screenHeight), null);
        }

        // 2️⃣ 再畫地板（從 GROUND_TOP_POSITION 開始）
        int gapHeight = gapBitmap != null ? gapBitmap.getHeight() : 0; // 取得未放大 Gap 的高度

        for (GroundTile tile : tiles) {
            // 確保 bitmap 存在
            if (tile.bitmap != null) {

                int currentTileHeight = tile.bitmap.getHeight();
                int currentGroundTop = GROUND_TOP_POSITION;

                // 🚀 關鍵修正 2: 如果是放大的 floorBitmap，調整其 Y 座標，使其底部與 Gap 對齊
                if (tile.bitmap == floorBitmap) {
                    // floorBitmap 的實際高度 (scaledFloorHeight) 比 Gap (groundHeight) 高
                    // 繪製的起點需要向上偏移 (scaledFloorHeight - gapHeight) 這麼多
                    int heightDifference = scaledFloorHeight - gapHeight;
                    currentGroundTop = GROUND_TOP_POSITION - heightDifference;
                }

                destRect.set(
                        tile.x,
                        currentGroundTop, // 使用調整後的 Y 座標
                        tile.x + TILE_WIDTH,
                        // 這裡使用 tile.bitmap.getHeight() 確保高度與縮放後一致
                        currentGroundTop + currentTileHeight
                );
                canvas.drawBitmap(tile.bitmap, null, destRect, null);
            }
        }
    }

    private void generateNextTile() {
        int nextX = tiles.getLast().x + TILE_WIDTH;

        Bitmap bm = (random.nextInt(100) < GAP_PERCENT_CHANCE)
                ? gapBitmap
                : floorBitmap;

        // 確保要生成的 bitmap 存在
        if (bm != null) {
            tiles.add(new GroundTile(bm, nextX));
        }
    }

    /**
     * 🚀 新增方法：檢查指定的 X 座標是否落在 Gap 區域內 (供 Candy Manager 查詢)
     * @param x 螢幕上的 X 座標
     * @return 如果 X 座標位於 Gap 的精準像素範圍內，返回 true
     */
    public boolean isXCoordinateGap(int x) {
        // 2. 檢查 X 軸：遍歷所有在畫面上的圖塊
        for (GroundTile tile : tiles) {
            // a. 檢查 X 座標是否落在這個圖塊的水平範圍內
            if (x >= tile.x && x < tile.x + TILE_WIDTH) {

                // b. 將精準判定委託給 Gap.java 處理
                return Gap.checkFalling(
                        x, // 檢查點的 X 座標
                        tile.bitmap,
                        tile.x,
                        gapBitmap,
                        TILE_WIDTH
                );
            }
        }
        // 如果 X 座標在任何圖塊之外
        return false;
    }


    /**
     * 檢查角色是否掉落的接口 (給組員 A 使用)
     * @param playerX 角色在螢幕上的 X 座標
     * @param playerY 角色在螢幕上的 Y 座標
     * @return 如果角色掉落，返回 true
     */
    public boolean isPlayerFalling(int playerX, int playerY) {
        // 1. 檢查 Y 軸：是否到達或超過碰撞點
        if (playerY < GROUND_COLLISION_Y) return false;

        // 2. 檢查 X 軸：遍歷所有在畫面上的圖塊
        for (GroundTile tile : tiles) {
            // a. 檢查 PlayerX 是否落在這個圖塊的水平範圍內
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {

                // b. 將精準判定委託給 Gap.java 處理
                return Gap.checkFalling(
                        playerX,
                        tile.bitmap,
                        tile.x,
                        gapBitmap,package com.example.project_group08.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import com.example.project_group08.R;
import com.example.project_group08.world.Gap; // 確保 Gap 類別已被正確引入

import java.util.LinkedList;
import java.util.Random;

public class Ground {

    // === 調整後的參數 ===
    private static final int TILE_WIDTH = 1024;    // 單一地板圖片寬
    private static final int SCROLL_SPEED = 10;
    private static final int GAP_PERCENT_CHANCE = 70; // 設為 70% 機率生成有洞圖塊

    // 🚀 新增：一般地板的額外縮放因子
    private static final float FLOOR_SCALE_FACTOR = 1.2f;

    public static int GROUND_TOP_POSITION;      // 地板開始 Y
    public static int GROUND_COLLISION_Y;       // 薑餅人腳底 Y（用於落地判斷）

    private Bitmap floorBitmap;
    private Bitmap gapBitmap;
    private Bitmap skyBitmap;

    // 新增：紀錄縮放後的 floorBitmap 高度
    private int scaledFloorHeight;

    private final int screenWidth;
    private final int screenHeight;

    private final LinkedList<GroundTile> tiles = new LinkedList<>();
    private final Random random = new Random();

    private final Rect destRect = new Rect();

    private class GroundTile {
        Bitmap bitmap;
        int x;
        GroundTile(Bitmap bm, int startX) {
            bitmap = bm;
            x = startX;
        }
    }

    public Ground(Context context, int screenW, int screenH) {
        screenWidth = screenW;
        screenHeight = screenH;

        // ⭐ 讓地板高度占畫面 40% (作為基礎高度)
        int groundHeight = (int)(screenH * 0.4f);

        // ⭐ 地板開始位置：從螢幕底部往上 groundHeight
        GROUND_TOP_POSITION = screenH - groundHeight;

        // ⭐ 薑餅人腳底碰撞高度（草皮上緣，適度往下）
        GROUND_COLLISION_Y = GROUND_TOP_POSITION + 180; // 保持相對位置

        // === 載入圖片 (已修正為 R.drawable 載入) ===
        Bitmap rawFloor = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        Bitmap rawGap   = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        Bitmap rawSky   = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // ⭐ 天空鋪滿整個畫布
        if (rawSky != null) {
            skyBitmap = Bitmap.createScaledBitmap(rawSky, screenW, screenH, true);
        } else {
            Log.e("Ground", "Sky bitmap failed to load. (sky.jpg)");
        }

        // ⭐ 地板依照固定高度縮放
        if (rawFloor != null) {
            // 🚀 關鍵修正 1: 計算額外放大後的 floorBitmap 高度
            scaledFloorHeight = (int)(groundHeight * FLOOR_SCALE_FACTOR);

            // 關鍵：floorBitmap 使用 scaledFloorHeight 進行縮放
            floorBitmap = Bitmap.createScaledBitmap(rawFloor, TILE_WIDTH, scaledFloorHeight, true);
        } else {
            Log.e("Ground", "Floor bitmap failed to load. (floor.jpg)");
        }

        if (rawGap != null) {
            // 關鍵：gapBitmap 使用基礎 groundHeight (未放大)
            gapBitmap   = Bitmap.createScaledBitmap(rawGap,   TILE_WIDTH, groundHeight, true);
        } else {
            Log.e("Ground", "Gap bitmap failed to load. (floor_w_hole.jpg)");
        }


        // === 初始化地板 tiles ===
        int x = 0;

        // 🚨 修正：確保第一個圖塊 (x=0) 永遠是普通地板 (FLOOR_TILE)
        tiles.add(new GroundTile(floorBitmap, x));
        x += TILE_WIDTH;

        // 確保後續圖塊填充直到填滿畫面
        while (x < screenW + TILE_WIDTH) {

            // 💡 填充後續圖塊時，也應該使用 floorBitmap
            tiles.add(new GroundTile(floorBitmap, x));
            x += TILE_WIDTH;
        }
    }

    public void update() {
        if (tiles.isEmpty()) return;

        // 移動
        for (GroundTile tile : tiles) {
            tile.x -= SCROLL_SPEED;
        }

        // 左邊移出畫面就刪除
        while (!tiles.isEmpty() && tiles.getFirst().x + TILE_WIDTH < 0) {
            tiles.removeFirst();
        }

        // 右邊補 tiles
        // 🚀 修正：應檢查最後一個圖塊是否滾入畫面即可生成下一個。
        while (!tiles.isEmpty() && tiles.getLast().x + TILE_WIDTH < screenWidth) {
            generateNextTile();
        }
    }

    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 1️⃣ 先畫天空（鋪滿整個畫面）
        if (skyBitmap != null) {
            canvas.drawBitmap(skyBitmap, null, new Rect(0, 0, screenWidth, screenHeight), null);
        }

        // 2️⃣ 再畫地板（從 GROUND_TOP_POSITION 開始）
        int gapHeight = gapBitmap != null ? gapBitmap.getHeight() : 0; // 取得未放大 Gap 的高度

        for (GroundTile tile : tiles) {
            // 確保 bitmap 存在
            if (tile.bitmap != null) {

                int currentTileHeight = tile.bitmap.getHeight();
                int currentGroundTop = GROUND_TOP_POSITION;

                // 🚀 關鍵修正 2: 如果是放大的 floorBitmap，調整其 Y 座標，使其底部與 Gap 對齊
                if (tile.bitmap == floorBitmap) {
                    // floorBitmap 的實際高度 (scaledFloorHeight) 比 Gap (groundHeight) 高
                    // 繪製的起點需要向上偏移 (scaledFloorHeight - gapHeight) 這麼多
                    int heightDifference = scaledFloorHeight - gapHeight;
                    currentGroundTop = GROUND_TOP_POSITION - heightDifference;
                }

                destRect.set(
                        tile.x,
                        currentGroundTop, // 使用調整後的 Y 座標
                        tile.x + TILE_WIDTH,
                        // 這裡使用 tile.bitmap.getHeight() 確保高度與縮放後一致
                        currentGroundTop + currentTileHeight
                );
                canvas.drawBitmap(tile.bitmap, null, destRect, null);
            }
        }
    }

    private void generateNextTile() {
        int nextX = tiles.getLast().x + TILE_WIDTH;

        Bitmap bm = (random.nextInt(100) < GAP_PERCENT_CHANCE)
                ? gapBitmap
                : floorBitmap;

        // 確保要生成的 bitmap 存在
        if (bm != null) {
            tiles.add(new GroundTile(bm, nextX));
        }
    }

    /**
     * 🚀 新增方法：檢查指定的 X 座標是否落在 Gap 區域內 (供 Candy Manager 查詢)
     * @param x 螢幕上的 X 座標
     * @return 如果 X 座標位於 Gap 的精準像素範圍內，返回 true
     */
    public boolean isXCoordinateGap(int x) {
        // 2. 檢查 X 軸：遍歷所有在畫面上的圖塊
        for (GroundTile tile : tiles) {
            // a. 檢查 X 座標是否落在這個圖塊的水平範圍內
            if (x >= tile.x && x < tile.x + TILE_WIDTH) {

                // b. 將精準判定委託給 Gap.java 處理
                return Gap.checkFalling(
                        x, // 檢查點的 X 座標
                        tile.bitmap,
                        tile.x,
                        gapBitmap,
                        TILE_WIDTH
                );
            }
        }
        // 如果 X 座標在任何圖塊之外
        return false;
    }


    /**
     * 檢查角色是否掉落的接口 (給組員 A 使用)
     * @param playerX 角色在螢幕上的 X 座標
     * @param playerY 角色在螢幕上的 Y 座標
     * @return 如果角色掉落，返回 true
     */
    public boolean isPlayerFalling(int playerX, int playerY) {
        // 1. 檢查 Y 軸：是否到達或超過碰撞點
        if (playerY < GROUND_COLLISION_Y) return false;

        // 2. 檢查 X 軸：遍歷所有在畫面上的圖塊
        for (GroundTile tile : tiles) {
            // a. 檢查 PlayerX 是否落在這個圖塊的水平範圍內
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {

                // b. 將精準判定委託給 Gap.java 處理
                return Gap.checkFalling(
                        playerX,
                        tile.bitmap,
                        tile.x,
                        gapBitmap,
                        TILE_WIDTH
                );
            }
        }
        return false;
    }
}

                        TILE_WIDTH
                );
            }
        }
        return false;
    }
}

