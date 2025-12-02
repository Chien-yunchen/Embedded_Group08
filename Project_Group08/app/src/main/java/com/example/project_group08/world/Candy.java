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
    private static final int CANDY_SIZE = 50; // 糖果顯示大小 (像素)
    private static final int SCROLL_SPEED = 10; // 應與 Ground.SCROLL_SPEED 保持一致

    // 糖果生成的位置範圍
    private static final int CANDY_Y_BASE_OFFSET = 100; // 糖果在 地板碰撞點上方 100 像素處
    private static final int ARCH_PEAK_OFFSET = 150; // 拱形最高點距離地板碰撞點上方 150 像素處
    private static final int ARCH_WIDTH = 400; // 拱形的水平寬度 (像素)

    // 生成機率
    private static final int SPAWN_DISTANCE = 300; // 每隔 300 像素生成一組糖果
    private static final int PATTERN_CHANCE = 75; // 75% 的機率生成糖果模式 (25% 機率不生成)
    private static final int ARCH_CHANCE_FLAT_GROUND = 20; // 平地時 20% 機率生成拱形
    private static final int ARCH_CHANCE_GAP_ZONE = 80; // 洞洞區 80% 機率生成拱形

    // --- 狀態 ---
    private final LinkedList<CandyItem> candies = new LinkedList<>();
    private final Random random = new Random();
    private Bitmap candyBitmap;
    private int screenWidth;
    private int lastSpawnX = 0; // 追蹤上次生成糖果的 X 座標

    // 根據 Ground.TILE_WIDTH=1024 設定
    private static final int GROUND_TILE_WIDTH = 1024;

    /**
     * 內部類別：代表單個糖果物件
     * 🚨 修正：新增 isCollected 狀態和方法
     */
    public class CandyItem {
        int x;
        int y;
        private boolean collected = false; // 新增：是否被收集的狀態
        final Rect destRect = new Rect();

        CandyItem(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Canvas canvas, Bitmap bitmap) {
            destRect.set(x, y, x + CANDY_SIZE, y + CANDY_SIZE);
            canvas.drawBitmap(bitmap, null, destRect, null);
        }

        // 組員 A 呼叫：檢查是否被收集
        public boolean isCollected() {
            return collected;
        }

        // 組員 B 呼叫：標記為已被收集
        public void setCollected(boolean collected) {
            this.collected = collected;
        }

        // 幫助組員 A 做碰撞判斷 (Getter)
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public Candy(Context context, int screenW, int screenH) {
        this.screenWidth = screenW;
        this.lastSpawnX = 0;

        // 載入糖果圖片 (確認 200x200 縮放為 50x50)
        Bitmap rawCandy = BitmapFactory.decodeResource(context.getResources(), R.drawable.candy);
        if (rawCandy != null) {
            candyBitmap = Bitmap.createScaledBitmap(rawCandy, CANDY_SIZE, CANDY_SIZE, true);
        } else {
            Log.e("Candy", "Candy bitmap (candy.png) failed to load. Check R.drawable.candy.");
        }

        // 初始生成點設定在第二塊地板開始處 (X=1024)，確保遊戲開始就有糖果
        this.lastSpawnX = GROUND_TILE_WIDTH;

        // 確保初始生成邏輯運行一次，避免遊戲開始時沒有糖果
        generateInitialCandy(context);
    }

    /**
     * 專門用於初始化，確保遊戲開始時第二塊地板有糖果
     */
    private void generateInitialCandy(Context context) {
        // 首次生成直線 (與 Ground 初始狀態的 FLOOR_TILE 匹配)
        int spawnX = GROUND_TILE_WIDTH + SPAWN_DISTANCE;
        int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET;

        spawnStraight(spawnX, startY);
        lastSpawnX = spawnX;
    }


    /**
     * 更新糖果位置並處理生成邏輯
     * 🚨 修正：現在 update 負責移除被收集和滾出螢幕的糖果
     *
     * @param ground Ground 實例
     */
    public void update(Ground ground) {
        // 1. 移動現有的糖果，並移除滾出螢幕或已被收集的糖果
        Iterator<CandyItem> it = candies.iterator();
        while (it.hasNext()) {
            CandyItem candy = it.next();
            candy.x -= SCROLL_SPEED;

            // 🚨 修正：移除被收集的糖果
            if (candy.isCollected()) {
                it.remove();
                continue; // 繼續檢查下一個
            }

            // 移除滾出螢幕左側的糖果
            if (candy.x + CANDY_SIZE < 0) {
                it.remove();
            }
        }

        // 2. 決定是否生成新的糖果
        // 只有當上一個生成點滾動到螢幕右側 SPAWN_DISTANCE 以外時，才嘗試生成
        if (lastSpawnX - SCROLL_SPEED < screenWidth + SPAWN_DISTANCE) {

            int spawnX = lastSpawnX + SPAWN_DISTANCE;

            // 查詢 Ground 是否為 Gap 區域。
            boolean isGapZone = ground.isXCoordinateGap(spawnX);

            // 如果不在 PATTERN_CHANCE 內，則不生成（偶爾不生成，實現要求）
            if (random.nextInt(100) < PATTERN_CHANCE) {

                int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET;

                if (isGapZone) {
                    // --- 洞口區 (Gap) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_GAP_ZONE) {
                        spawnArch(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH);
                    } else {
                        // 不生成 (實現 "有洞時不生成" 的部分要求)
                    }

                } else {
                    // --- 平地區 (Floor) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_FLAT_GROUND) {
                        spawnArch(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH);
                    } else {
                        // 直線生成 (大部分情況)
                        spawnStraight(spawnX, startY);
                    }
                }
            }

            lastSpawnX = spawnX;
        }
    }

    // 獨立生成方法：生成直線 (平地)
    private void spawnStraight(int startX, int startY) {
        int count = random.nextInt(4) + 4;
        int spacing = 80;
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
            // 只繪製未被收集的糖果
            if (!candy.isCollected()) {
                candy.draw(canvas, candyBitmap);
            }
        }
    }

    /**
     * 檢查角色是否碰到糖果 (給 Player.java 呼叫)
     * 🚨 修正：將碰撞到的糖果標記為 Collected，不再從主列表移除
     *
     * @param playerRect 玩家角色的 Rect 邊界
     * @return 碰到的糖果列表 (以便 Player 移除它)
     */
    public LinkedList<CandyItem> setCollected(Rect playerRect) {
        if (candyBitmap == null) return new LinkedList<>();

        LinkedList<CandyItem> collected = new LinkedList<>();
        for (CandyItem candy : candies) {
            // 只有未被收集的糖果才需要檢查碰撞
            if (!candy.isCollected() && playerRect.intersects(candy.x, candy.y, candy.x + CANDY_SIZE, candy.y + CANDY_SIZE)) {

                candy.setCollected(true); // 標記為已被收集
                collected.add(candy);
            }
        }
        return collected;
    }
}
