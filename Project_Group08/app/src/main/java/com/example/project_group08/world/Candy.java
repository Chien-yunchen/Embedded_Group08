package com.example.project_group08.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.example.project_group08.R;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * 組員任務：負責生成、移動和繪製遊戲中的所有糖果 (Candy)。
 */
public class Candy {

    // --- 遊戲常數 ---
    private static final int CANDY_SIZE = 50;      // 糖果顯示大小 (像素)
    private static final int SCROLL_SPEED = 10;    // 應與 Ground.SCROLL_SPEED 保持一致

    // 糖果生成的位置範圍
    private static final int CANDY_Y_BASE_OFFSET = 100;   // 糖果在 地板碰撞點上方 100 像素處
    private static final int ARCH_PEAK_OFFSET = 150;      // 拱形最高點距離地板碰撞點上方 150 像素處
    private static final int ARCH_WIDTH = 400;            // 拱形的水平寬度 (像素)

    // 生成機率（可以自己微調）
    private static final int SPAWN_DISTANCE = 500;        // ⭐ 每隔 500 像素生成一組糖果（變比較稀疏）
    private static final int PATTERN_CHANCE = 75;         // 75% 的機率生成糖果模式 (25% 機率不生成)
    private static final int ARCH_CHANCE_FLAT_GROUND = 20; // 平地時 20% 機率生成拱形
    private static final int ARCH_CHANCE_GAP_ZONE = 80;    // 洞洞區 80% 機率生成拱形

    // --- 狀態 ---
    private final LinkedList<CandyItem> candies = new LinkedList<>();
    private final Random random = new Random();
    private Bitmap candyBitmap;
    private int screenWidth;

    // ⭐ 改成用「距離」來決定何時生成下一批糖果
    private int distanceSinceLastSpawn = 0;

    /**
     * 內部類別：代表單個糖果物件
     */
    public class CandyItem {
        int x;
        int y;
        private boolean collected = false; // 是否被收集的狀態
        final Rect destRect = new Rect();

        CandyItem(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Canvas canvas, Bitmap bitmap) {
            destRect.set(x, y, x + CANDY_SIZE, y + CANDY_SIZE);
            canvas.drawBitmap(bitmap, null, destRect, null);
        }

        public boolean isCollected() {
            return collected;
        }

        public void setCollected(boolean collected) {
            this.collected = collected;
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    public Candy(Context context, int screenW, int screenH) {
        this.screenWidth = screenW;

        // 載入糖果圖片
        Bitmap rawCandy = BitmapFactory.decodeResource(context.getResources(), R.drawable.candy);
        if (rawCandy != null) {
            candyBitmap = Bitmap.createScaledBitmap(rawCandy, CANDY_SIZE, CANDY_SIZE, true);
        } else {
            Log.e("Candy", "Candy bitmap (candy.png) failed to load. Check R.drawable.candy.");
        }

        // ⭐ 一開始就先在畫面中間附近生成一小排糖果，讓玩家一開始就看得到
        generateInitialCandy();
    }

    /**
     * 專門用於初始化，確保遊戲開始時畫面上就有糖果
     */
    private void generateInitialCandy() {
        int spawnX = screenWidth / 2;  // 在畫面中間右邊一點
        int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET;
        spawnStraight(spawnX, startY);
        distanceSinceLastSpawn = 0;    // 重置距離累積
    }

    /**
     * 更新糖果位置並處理生成邏輯
     *
     * @param ground Ground 實例
     */
    public void update(Ground ground) {
        // 1. 移動現有的糖果，並移除滾出螢幕或已被收集的糖果
        Iterator<CandyItem> it = candies.iterator();
        while (it.hasNext()) {
            CandyItem candy = it.next();
            candy.x -= SCROLL_SPEED;

            // 移除被收集的糖果
            if (candy.isCollected()) {
                it.remove();
                continue;
            }

            // 移除滾出螢幕左側的糖果
            if (candy.x + CANDY_SIZE < 0) {
                it.remove();
            }
        }

        // 2. 累積捲動距離
        distanceSinceLastSpawn += SCROLL_SPEED;

        // 每累積超過 SPAWN_DISTANCE，就嘗試生成一組新的糖果
        if (distanceSinceLastSpawn >= SPAWN_DISTANCE) {

            // 在螢幕右邊外面一點生成（慢慢滑進畫面）
            int spawnX = screenWidth + 50;

            // 查詢 Ground：這個 X 大致上是不是洞洞區
            boolean isGapZone = (ground != null) && ground.isXCoordinateGap(spawnX);

            // 只有在 PATTERN_CHANCE 範圍內才真的生成糖果
            if (random.nextInt(100) < PATTERN_CHANCE) {

                int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET;

                if (isGapZone) {
                    // --- 洞口區 (Gap) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_GAP_ZONE) {
                        // 在洞洞上方生成拱形糖果
                        spawnArch(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH);
                    } else {
                        // 不生成（保留一些空白區）
                    }

                } else {
                    // --- 平地區 (Floor) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_FLAT_GROUND) {
                        // 偶爾在平地也來一個拱形
                        spawnArch(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH);
                    } else {
                        // 大部分情況生成一條直線糖果
                        spawnStraight(spawnX, startY);
                    }
                }
            }

            // 重置距離累積
            distanceSinceLastSpawn = 0;
        }
    }

    // 獨立生成方法：生成直線 (平地)
    private void spawnStraight(int startX, int startY) {
        int count = random.nextInt(4) + 4;  // 4~7 顆
        int spacing = 80;                   // 每顆間隔 80 像素
        for (int i = 0; i < count; i++) {
            candies.add(new CandyItem(startX + i * spacing, startY));
        }
    }

    // 獨立生成方法：生成拱形 (跳躍路徑)
    private void spawnArch(int startX, int peakY, int archWidth) {
        int steps = 4;
        int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET; // 拱形起始 Y 座標

        // 拋物線方程：y = A * (x - h)^2 + k
        int h = startX + archWidth / 2;
        int k = peakY; // 頂點 Y

        // A 的計算：當 x = startX 時，y = startY
        float A = (startY - k) / (float) Math.pow(startX - h, 2);

        for (int i = 0; i <= steps; i++) {
            int currentX = startX + (archWidth / steps) * i;
            int currentY = (int) (A * Math.pow(currentX - h, 2) + k);

            candies.add(new CandyItem(currentX, currentY));
        }
    }

    /**
     * 繪製所有糖果
     */
    public void draw(Canvas canvas) {
        if (candyBitmap == null) return;
        for (CandyItem candy : candies) {
            if (!candy.isCollected()) {
                candy.draw(canvas, candyBitmap);
            }
        }
    }

    /**
     * 檢查角色是否碰到糖果
     *
     * @param playerRect 玩家角色的 Rect 邊界
     * @return 碰到的糖果列表
     */
    public LinkedList<CandyItem> setCollected(Rect playerRect) {
        LinkedList<CandyItem> collected = new LinkedList<>();
        if (candyBitmap == null) return collected;

        for (CandyItem candy : candies) {
            if (!candy.isCollected()
                    && playerRect.intersects(candy.x, candy.y,
                    candy.x + CANDY_SIZE,
                    candy.y + CANDY_SIZE)) {

                candy.setCollected(true);
                collected.add(candy);
            }
        }
        return collected;
    }
}
