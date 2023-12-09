package com.misoca12.aisample

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Fruit(
    val displayName: String,
    val color: Color,
    val size: Dp,
    val textSize: TextUnit
) {
    WATERMELON("スイカ", Color(0xFF30671F), 400.dp, 200.sp),
    MELON("メロン", Color(0xFF87B83D), 300.dp, 160.sp),
    PINEAPPLE("パイナップル", Color(0xFFF1D248), 240.dp, 120.sp),
    PEACH("モモ", Color(0xFFF5C9C1), 180.dp, 80.sp),
    PEAR("ナシ", Color(0xFFFBF189), 120.dp, 60.sp),
    APPLE("リンゴ", Color(0xFFE2372A), 100.dp, 50.sp),
    KAKI("カキ", Color(0xFFEE8D39), 80.dp, 40.sp),
    DEKOPON("デコポン", Color(0xFFF4BA40), 60.dp, 30.sp),
    GRAPE("ブドウ", Color(0xFF5913E5), 48.dp, 20.sp),
    STRAWBERRY("イチゴ", Color(0xFFEC7355), 36.dp, 14.sp),
    CHERRY("サクランボ", Color(0xFFDF3325), 24.dp, 10.sp);

    companion object {
        fun random(): Fruit {
            val excluded = setOf(
                WATERMELON,
                MELON,
                PINEAPPLE,
                PEACH,
                PEAR,
                APPLE,
            )
//            val excluded = emptySet<Fruit>()
            return entries.filter { it !in excluded }.random()
        }
    }

    fun rankup(fruit: Fruit) = when(fruit) {
        CHERRY -> STRAWBERRY
        STRAWBERRY -> GRAPE
        GRAPE -> DEKOPON
        DEKOPON -> KAKI
        KAKI -> APPLE
        APPLE -> PEAR
        PEAR -> PEACH
        PEACH -> PINEAPPLE
        PINEAPPLE -> MELON
        MELON -> WATERMELON
        WATERMELON -> null
    }
}