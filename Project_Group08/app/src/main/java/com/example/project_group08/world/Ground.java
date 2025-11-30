package com.example.project_group08.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.project_group08.R;

import java.util.LinkedList;
import java.util.Random;

public class Ground {

    // === 調整後的參數 ===
    private static final int TILE_WIDTH = 1024;    // 單一地板圖片寬
    private static final int SCROLL_SPEED = 10;
    private static final int GAP_PERCENT_CHANCE = 70;

    public static int GROUND_TOP_POSITION;      // 地板開始 Y
    public static int GROUND_COLLISION_Y;       // 薑餅人腳底 Y（用於落地判斷）

    private Bitmap floorBitmap;
    private Bitmap gapBitmap;
    private Bitmap skyBitmap;

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

        // ⭐ 讓地板高度占畫面 35%（比以前自然）
        int groundHeight = (int)(screenH * 0.35f);

        // ⭐ 地板開始位置：從螢幕底部往上 groundHeight
        GROUND_TOP_POSITION = screenH - groundHeight;

        // ⭐ 薑餅人腳底碰撞高度（草皮上緣，適度往下）
        GROUND_COLLISION_Y = GROUND_TOP_POSITION + 60;

        // === 載入圖片 ===
        Bitmap rawFloor = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        Bitmap rawGap   = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        Bitmap rawSky   = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // ⭐ 天空直接鋪滿整個畫布（最重要：這樣永遠不會有黑縫）
        skyBitmap = Bitmap.createScaledBitmap(rawSky, screenW, screenH, true);

        // ⭐ 地板依照固定高度縮放
        floorBitmap = Bitmap.createScaledBitmap(rawFloor, TILE_WIDTH, groundHeight, true);
        gapBitmap   = Bitmap.createScaledBitmap(rawGap,   TILE_WIDTH, groundHeight, true);

        // === 初始化地板 tiles ===
        int x = 0;
        while (x < screenW + TILE_WIDTH) {
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
        while (!tiles.isEmpty() && tiles.getLast().x + TILE_WIDTH < screenWidth + TILE_WIDTH) {
            generateNextTile();
        }
    }

    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 1️⃣ 先畫天空（鋪滿整個畫面，不會有黑縫）
        canvas.drawBitmap(skyBitmap, null, new Rect(0, 0, screenWidth, screenHeight), null);

        // 2️⃣ 再畫地板（從 GROUND_TOP_POSITION 開始）
        for (GroundTile tile : tiles) {
            destRect.set(
                    tile.x,
                    GROUND_TOP_POSITION,
                    tile.x + TILE_WIDTH,
                    GROUND_TOP_POSITION + floorBitmap.getHeight()
            );
            canvas.drawBitmap(tile.bitmap, null, destRect, null);
        }
    }

    private void generateNextTile() {
        int nextX = tiles.getLast().x + TILE_WIDTH;

        Bitmap bm = (random.nextInt(100) < GAP_PERCENT_CHANCE)
                ? gapBitmap
                : floorBitmap;

        tiles.add(new GroundTile(bm, nextX));
    }

    public boolean isPlayerFalling(int playerX, int playerY) {
        if (playerY < GROUND_COLLISION_Y) return false;

        for (GroundTile tile : tiles) {
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {
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
