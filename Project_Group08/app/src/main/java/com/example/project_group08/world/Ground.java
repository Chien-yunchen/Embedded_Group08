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

    // 地板圖片的實際高度（依照你的 floor 圖來）
    private static final int GROUND_HEIGHT = 747;
    private static final int TILE_WIDTH = 1024;
    private static final int SCROLL_SPEED = 10;
    private static final int GAP_PERCENT_CHANCE = 70;

    // ★ 地板頂端（畫地板用）
    public static int GROUND_TOP_POSITION;

    // ★ 角色腳底碰撞高度（讓腳剛好貼在草皮上，可以微調）
    public static int GROUND_COLLISION_Y;

    private Bitmap floorBitmap;
    private Bitmap gapBitmap;
    private Bitmap skyBitmap;

    private final int screenWidth;
    private final int screenHeight;

    private final LinkedList<GroundTile> tiles = new LinkedList<>();
    private final Random random = new Random();

    private final Rect skyRect = new Rect();
    private final Rect destRect = new Rect();

    private class GroundTile {
        Bitmap bitmap;
        int x;
        GroundTile(Bitmap bm, int startX) {
            this.bitmap = bm;
            this.x = startX;
        }
    }

    public Ground(Context context, int screenW, int screenH) {
        this.screenWidth = screenW;
        this.screenHeight = screenH;

        // 1️⃣ 先決定地板要畫在哪裡：
        //    讓整塊地板剛好貼到底部
        GROUND_TOP_POSITION = screenH - GROUND_HEIGHT;

        // 2️⃣ 草皮不在整張圖的最底下，大概再往下移一點點
        //    這裡先抓一個估計值（你之後可以微調這個 80）
        int grassOffset = 80;   // 草皮厚度估計
        GROUND_COLLISION_Y = GROUND_TOP_POSITION + grassOffset;

        // 讀圖片
        Bitmap rawFloor = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        Bitmap rawGap   = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        Bitmap rawSky   = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // 地板縮放成固定高度
        floorBitmap = Bitmap.createScaledBitmap(rawFloor, TILE_WIDTH, GROUND_HEIGHT, true);
        gapBitmap   = Bitmap.createScaledBitmap(rawGap,   TILE_WIDTH, GROUND_HEIGHT, true);

        // 天空依比例縮放成螢幕寬，高度維持比例（不被壓扁）
        float skyScale = screenW / (float) rawSky.getWidth();
        int skyH = (int) (rawSky.getHeight() * skyScale);
        skyBitmap = Bitmap.createScaledBitmap(rawSky, screenW, skyH, true);

        // 預先填滿地板
        int currentX = 0;
        while (currentX < screenW + TILE_WIDTH) {
            tiles.add(new GroundTile(floorBitmap, currentX));
            currentX += TILE_WIDTH;
        }
    }

    public void update() {
        if (tiles.isEmpty()) return;

        for (GroundTile tile : tiles) {
            tile.x -= SCROLL_SPEED;
        }

        while (!tiles.isEmpty() && tiles.getFirst().x + TILE_WIDTH < 0) {
            tiles.removeFirst();
        }

        while (!tiles.isEmpty() && tiles.getLast().x + TILE_WIDTH < screenWidth + TILE_WIDTH) {
            generateNextTile();
        }
    }

    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 天空畫到 GROUND_TOP_POSITION（中間不會有黑縫）
        skyRect.set(0, 0, screenWidth, GROUND_TOP_POSITION);
        canvas.drawBitmap(skyBitmap, null, skyRect, null);

        // 地板從 GROUND_TOP_POSITION 開始往下畫
        for (GroundTile tile : tiles) {
            if (tile.bitmap == null) continue;
            if (tile.x >= screenWidth || tile.x + TILE_WIDTH <= 0) continue;

            destRect.set(
                    tile.x,
                    GROUND_TOP_POSITION,
                    tile.x + TILE_WIDTH,
                    GROUND_TOP_POSITION + GROUND_HEIGHT
            );
            canvas.drawBitmap(tile.bitmap, null, destRect, null);
        }
    }

    private void generateNextTile() {
        int lastX = tiles.getLast().x + TILE_WIDTH;
        Bitmap bm = (random.nextInt(100) < GAP_PERCENT_CHANCE) ? gapBitmap : floorBitmap;
        tiles.add(new GroundTile(bm, lastX));
    }

    public boolean isPlayerFalling(int playerX, int playerY) {
        // 腳底還在草皮線之上就不可能掉
        if (playerY < GROUND_COLLISION_Y) return false;

        for (GroundTile tile : tiles) {
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {
                if (Gap.checkFalling(playerX, tile.bitmap, tile.x, gapBitmap, TILE_WIDTH)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
