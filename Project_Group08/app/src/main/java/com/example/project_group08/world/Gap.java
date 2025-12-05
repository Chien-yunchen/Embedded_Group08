package com.example.project_group08.world;

import android.graphics.Bitmap;

/**
 * 處理 Gap的常數定義與掉落判定邏輯。
 */
public class Gap {

    // 洞洞在圖片上的精準位置 (像素級別偵測)
    public static final int GAP_START_PIXEL = 355; // 洞在圖片上的起始 X 座標
    public static final int GAP_END_PIXEL = 597;   // 洞在圖片上的結束 X 座標

    /**
     * 檢查角色是否落在指定圖塊的精準洞區間內。
     * @param playerX 角色在螢幕上的 X 座標
     * @param tile 正在檢查的 GroundTile 物件 (需確保 GroundTile 結構對外可見，或在 Ground 中傳遞所需參數)
     * @param gapBitmap 有洞圖塊的 Bitmap 實例
     * @param tileWidth 圖塊的寬度
     * @return 如果角色掉落，返回 true
     */
    public static boolean checkFalling(int playerX, Object tile, Bitmap gapBitmap, int tileWidth) {
        return checkFalling(playerX, (Bitmap) ((Object[])tile)[0], (int) ((Object[])tile)[1], gapBitmap, tileWidth);
    }

    public static boolean checkFalling(int playerX, Bitmap tileBitmap, int tileX, Bitmap gapBitmap, int tileWidth) {

        // 檢查這個圖塊是否是【有洞的圖片】
        if (tileBitmap == gapBitmap) {

            // 計算 PlayerX 相對於這個圖塊的「內部像素座標」
            int playerRelativeX = playerX - tileX;

            // 【精準判定】PlayerX 是否落入圖片上定義的洞的範圍 (GAP_START_PIXEL 到 GAP_END_PIXEL)
            if (playerRelativeX >= GAP_START_PIXEL && playerRelativeX < GAP_END_PIXEL) {

                // 角色落在了有洞圖塊的洞區間內，判定掉落
                return true;
            }
        }
        return false;
    }
}