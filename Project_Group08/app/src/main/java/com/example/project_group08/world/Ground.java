package com.example.project_group08.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.project_group08.R;

import java.util.LinkedList;
import java.util.Random;

/**
 * 組員 B 任務：處理地板滾動、圖塊生成以及作為掉落判定的主要接口。
 */
public class Ground {

    // --- B 必須提供的常數：給 A 組員使用 ---
    // 這裡是地板「頂端」的 Y 座標（上面是天空，下面是地板）
    public static final int GROUND_TOP_POSITION = 240;

    // --- 遊戲設定 ---
    // 每幀往左移動的像素數，配合 GameThread 的 FPS（大約 60fps）
    private static final int SCROLL_SPEED = 10;

    // 單一地板圖塊的寬、高（會用來把圖片預縮放到這個大小）
    private static final int TILE_WIDTH = 1024;
    private static final int GROUND_HEIGHT = 747;

    // 洞洞生成的機率 (0~100)
    private static final int GAP_PERCENT_CHANCE = 70;

    // --- 素材資源（已經縮放好的版本） ---
    private Bitmap floorBitmap;     // 完整地板圖（已縮放）
    private Bitmap gapBitmap;       // 有洞地板圖（已縮放）
    private Bitmap skyBitmap;       // 天空背景圖（已縮放）

    // --- 滾動狀態 ---
    private final LinkedList<GroundTile> tiles = new LinkedList<>();
    private final int screenWidth;
    private final int screenHeight;
    private final Random random = new Random();

    // 重用的 Rect，避免每幀 new 物件
    private final Rect skyRect = new Rect();
    private final Rect destRect = new Rect();

    // 地板區塊
    private class GroundTile {
        Bitmap bitmap;  // floor 或 gap
        int x;          // 左上角 X

        GroundTile(Bitmap bm, int startX) {
            this.bitmap = bm;
            this.x = startX;
        }
    }

    public Ground(Context context, int screenW, int screenH) {
        this.screenWidth = screenW;
        this.screenHeight = screenH;

        // 讀取原始圖片
        Bitmap rawFloor = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor);
        Bitmap rawGap   = BitmapFactory.decodeResource(context.getResources(), R.drawable.floor_w_hole);
        Bitmap rawSky   = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky);

        // ★ 只在這裡縮放一次，之後畫圖就不用再做縮放計算 ★
        floorBitmap = Bitmap.createScaledBitmap(rawFloor, TILE_WIDTH, GROUND_HEIGHT, true);
        gapBitmap   = Bitmap.createScaledBitmap(rawGap,   TILE_WIDTH, GROUND_HEIGHT, true);
        skyBitmap   = Bitmap.createScaledBitmap(rawSky,   screenWidth, GROUND_TOP_POSITION, true);

        // 原始 bitmap 用不到了，可以交給 GC（rawFloor/rawGap/rawSky 自己會被回收）

        // 預先把畫面填滿地板
        int currentX = 0;
        while (currentX < screenWidth + TILE_WIDTH) {
            tiles.add(new GroundTile(floorBitmap, currentX));
            currentX += TILE_WIDTH;
        }
    }

    // === 每幀更新：移動地板 + 移除超出畫面 + 在右邊補新的 ===
    public void update() {
        if (tiles.isEmpty()) return;

        // 1. 移動所有地板區塊
        for (GroundTile tile : tiles) {
            tile.x -= SCROLL_SPEED;
        }

        // 2. 移除完全跑出畫面的最左邊那塊
        //    （x + TILE_WIDTH < 0 表示整塊都在螢幕左邊外面）
        while (!tiles.isEmpty() && tiles.getFirst().x + TILE_WIDTH < 0) {
            tiles.removeFirst();
        }

        // 3. 在右邊補新的地板，確保畫面右邊永遠有地板接上來
        //    這裡多補一個 TILE_WIDTH 當緩衝，避免邊界閃一下
        while (!tiles.isEmpty() && tiles.getLast().x + TILE_WIDTH < screenWidth + TILE_WIDTH) {
            generateNextTile();
        }
    }

    // === 繪製：先畫天空，再畫目前在畫面內的地板 ===
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 1. 畫天空（固定在上半部）
        skyRect.set(0, 0, screenWidth, GROUND_TOP_POSITION);
        canvas.drawBitmap(skyBitmap, null, skyRect, null);

        // 2. 畫地板
        for (GroundTile tile : tiles) {
            if (tile.bitmap == null) continue;

            // 只畫在畫面範圍有交集的部分
            if (tile.x >= screenWidth || tile.x + TILE_WIDTH <= 0) {
                continue;
            }

            destRect.set(
                    tile.x,
                    GROUND_TOP_POSITION,
                    tile.x + TILE_WIDTH,
                    GROUND_TOP_POSITION + GROUND_HEIGHT
            );
            canvas.drawBitmap(tile.bitmap, null, destRect, null);
        }
    }

    // === 生成下一塊地板（可能是有洞的） ===
    private void generateNextTile() {
        int lastX = tiles.getLast().x + TILE_WIDTH;

        // 用隨機數決定這塊是不是有洞
        Bitmap bm;
        if (random.nextInt(100) < GAP_PERCENT_CHANCE) {
            bm = gapBitmap;
        } else {
            bm = floorBitmap;
        }

        tiles.add(new GroundTile(bm, lastX));
    }

    /**
     * 供組員 A 判斷角色是否掉落的接口
     * 核心邏輯：將精準判定委託給 Gap.java 處理。
     *
     * @param playerX 角色 X（通常是腳的 X）
     * @param playerY 角色 Y（通常是腳的 Y）
     */
    public boolean isPlayerFalling(int playerX, int playerY) {
        // 1. 角色還在天空上方就不可能掉到洞裡
        if (playerY < GROUND_TOP_POSITION) {
            return false;
        }

        // 2. 找出角色目前站在哪一塊地板圖塊上
        for (GroundTile tile : tiles) {
            if (tile.bitmap == null) continue;

            // 檢查 PlayerX 是否在這塊圖塊的水平範圍內
            if (playerX >= tile.x && playerX < tile.x + TILE_WIDTH) {

                // 如果這塊是有洞的圖，就進一步用 Gap.checkFalling 精確判斷
                if (Gap.checkFalling(playerX, tile.bitmap, tile.x, gapBitmap, TILE_WIDTH)) {
                    // 落在洞區間 → 掉落
                    return true;
                }

                // 在任何一塊實體地板上（不管是普通地板還是有洞圖的實心部分）→ 安全
                return false;
            }
        }

        // 沒踩在任何地板上（理論上不會發生），這裡當作沒掉落
        return false;
    }
}
