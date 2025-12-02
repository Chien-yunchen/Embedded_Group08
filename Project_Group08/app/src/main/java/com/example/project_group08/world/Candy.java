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
    
    // 🚨 測試常數 🚨
    private static final int TEST_Y_OFFSET = 200; // 測試時糖果在上方 200 像素處

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
    
    // 🚀 新增旗標：確保第一次生成在正確位置
    private boolean isFirstSpawn = true; 
    private static final int GROUND_TILE_WIDTH = 1024; // 根據 Ground.TILE_WIDTH=1024 設定

    /**
     * 內部類別：代表單個糖果物件
     */
    public class CandyItem {
        int x;
        int y;
        final Rect destRect = new Rect();

        CandyItem(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Canvas canvas, Bitmap bitmap) {
            destRect.set(x, y, x + CANDY_SIZE, y + CANDY_SIZE);
            canvas.drawBitmap(bitmap, null, destRect, null);
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
    }

    /**
     * 更新糖果位置並處理生成邏輯
     * @param ground Ground 實例
     */
    public void update(Ground ground) {
        // 1. 移動現有的糖果
        Iterator<CandyItem> it = candies.iterator();
        while (it.hasNext()) {
            CandyItem candy = it.next();
            candy.x -= SCROLL_SPEED;
            // 移除滾出螢幕左側的糖果
            if (candy.x + CANDY_SIZE < 0) {
                it.remove();
            }
        }
        
        // 🚀 新增：連續 Log 第一顆糖果的位置，用於測試滾動
        if (!candies.isEmpty()) {
            CandyItem firstCandy = candies.getFirst();
            Log.d("CANDY_MOVEMENT", String.format("X=%d | Y=%d", firstCandy.x, firstCandy.y));
        }
        
        // 2. 決定是否生成新的糖果
        int spawnX = lastSpawnX + SPAWN_DISTANCE;
        
        if (isFirstSpawn) {
            // 🚀 測試模式：強制在第三塊地板的起始點 (X=2048) 生成拱形，位於地板上方 200 像素
            spawnX = GROUND_TILE_WIDTH * 2; 
            
            int startY = Ground.GROUND_COLLISION_Y - TEST_Y_OFFSET;
            
            // 🚨 強制生成拱形 pattern
            spawnArchPattern(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH, startY);

            Log.d("CANDY_TEST", String.format("FORCED ARCH spawn at X=%d, Y_Base=%d", spawnX, startY));
            
            lastSpawnX = spawnX;
            isFirstSpawn = false; // 測試模式只運行一次
        
        } else if (lastSpawnX - SCROLL_SPEED < screenWidth + SPAWN_DISTANCE) {
            // 🚀 連續生成 (恢復正常遊戲邏輯)
            
            // 查詢 Ground 是否為 Gap 區域。
            boolean isGapZone = ground.isXCoordinateGap(spawnX);
            
            Log.d("CANDY_SPAWN", "Attempting spawn at X=" + spawnX + " (GapZone: " + isGapZone + ")"); 

            // 如果不在 PATTERN_CHANCE 內，則不生成（偶爾不生成，實現要求）
            if (random.nextInt(100) < PATTERN_CHANCE) {
                
                int startY = Ground.GROUND_COLLISION_Y - CANDY_Y_BASE_OFFSET;

                if (isGapZone) {
                    // --- 洞口區 (Gap) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_GAP_ZONE) {
                        spawnArchPattern(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH, startY);
                        Log.d("CANDY_SPAWN", "Gap zone: Spawned ARCH pattern."); 
                    } else {
                        Log.d("CANDY_SPAWN", "Gap zone: CHANCE failed (20% skip). No candy spawned."); 
                    }

                } else {
                    // --- 平地區 (Floor) 生成邏輯 ---
                    if (random.nextInt(100) < ARCH_CHANCE_FLAT_GROUND) {
                        spawnArchPattern(spawnX, Ground.GROUND_COLLISION_Y - ARCH_PEAK_OFFSET, ARCH_WIDTH, startY);
                        Log.d("CANDY_SPAWN", "Flat zone: Spawned ARCH pattern."); 
                    } else {
                        // 直線生成 (大部分情況)
                        spawnStraight(spawnX, startY);
                        Log.d("CANDY_SPAWN", "Flat zone: Spawned STRAIGHT pattern."); 
                    }
                }
            } else {
                Log.d("CANDY_SPAWN", "Skip spawn due to PATTERN_CHANCE (25% skip)."); 
            }

            lastSpawnX = spawnX;
        }
    }

    // 獨立生成方法：生成直線 (平地)
    private void spawnStraight(int startX, int startY) {
        int count = random.nextInt(4) + 4; // 4到7個
        int spacing = 80;
        for (int i = 0; i < count; i++) {
            candies.add(new CandyItem(startX + i * spacing, startY));
        }
    }

    // 獨立生成方法：生成拱形 (跳躍路徑) - 抽離邏輯以便測試和遊戲邏輯共用
    private void spawnArchPattern(int startX, int peakY, int archWidth, int startY) {
        // 5 個糖果 (0, 1, 2, 3, 4)，所以 steps = 4
        int steps = 4;
        
        // 拋物線方程：y = A * (x - h)^2 + k
        int h = startX + archWidth / 2;
        int k = peakY; // 頂點 Y
        
        // A 的計算：當 x = startX 時，y = startY
        float A = (startY - k) / (float) Math.pow(startX - h, 2);

        for (int i = 0; i <= steps; i++) {
            // 確保糖果水平間隔均勻
            int currentX = startX + (archWidth / steps) * i;

            // 計算拋物線 Y 座標
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
            candy.draw(canvas, candyBitmap);
        }
    }

    /**
     * 檢查角色是否碰到糖果 (給 Player.java 呼叫)
     * @param playerRect 玩家角色的 Rect 邊界
     * @return 碰到的糖果列表 (以便 Player 移除它)
     */
    public LinkedList<CandyItem> checkCollection(Rect playerRect) {
        if (candyBitmap == null) return new LinkedList<>();

        LinkedList<CandyItem> collected = new LinkedList<>();
        Iterator<CandyItem> it = candies.iterator();
        while (it.hasNext()) {
            CandyItem candy = it.next();
            // 簡單的矩形碰撞檢查
            if (playerRect.intersects(candy.x, candy.y, candy.x + CANDY_SIZE, candy.y + CANDY_SIZE)) {
                collected.add(candy);
                // 🚀 關鍵：在這裡直接移除，實現「碰到就啟用移除」
                it.remove(); 
            }
        }
        return collected;
    }

    /**
     * 移除指定的糖果 (Player 收集後呼叫)
     * @param candy 被收集的 CandyItem 物件
     */
    public void removeCandy(CandyItem candy) {
        // 雖然上面的 checkCollection 已經移除了，但保留這個方法供外部直接操作 (例如在 Player 類別中)
        candies.remove(candy);
    }
}
