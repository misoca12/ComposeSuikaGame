package com.misoca12.composesuikagame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.misoca12.composesuikagame.ui.theme.ComposeSuikaGameTheme
import de.apuri.physicslayout.lib.BodyConfig
import de.apuri.physicslayout.lib.PhysicsLayout
import de.apuri.physicslayout.lib.drag.DragConfig
import de.apuri.physicslayout.lib.physicsBody
import de.apuri.physicslayout.lib.simulation.rememberSimulation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private val DEFAULT_SCALE = 32.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeSuikaGameTheme {
                GameScreen()
            }
        }
    }
}

@Composable
fun GameScreen() {
    var fruitCounter by remember { mutableStateOf(0) }
    val fruits = remember { mutableStateListOf<FruitMeta>() }

    val density = LocalDensity.current
    val scalePx = density.run { DEFAULT_SCALE.toPx().toDouble() }

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    fruits.add(FruitMeta("fruit-${fruitCounter++}", Fruit.random(), it))
                }
            )
        },
        color = MaterialTheme.colorScheme.background
    ) {
        val simulation = rememberSimulation()

        GravitySensor { (x, y) ->
            simulation.setGravity(Offset(-x, y).times(3f))
        }

        PhysicsLayout(
            modifier = Modifier.systemBarsPadding(),
            simulation = simulation,
            content = {
                fruits.forEach { fruitMeta ->
                    key(fruitMeta.id) {
                        FruitObject(
                            fruitMeta
                        ) { id ->
                            // アイテムタップ時にID取得
                        }
                    }
                }
            },
            onCollision = { ids: Pair<String, String>, offset: Pair<Double, Double> ->
                val fruit1 = fruits.firstOrNull { it.id == ids.first } ?: return@PhysicsLayout
                val fruit2 = fruits.firstOrNull { it.id == ids.second } ?: return@PhysicsLayout
                if (fruit1.fruit == fruit2.fruit) {
                    fruits.removeIf { it.id == fruit1.id }
                    fruits.removeIf { it.id == fruit2.id }
                    val merged = fruit1.fruit.rankup() ?: return@PhysicsLayout
                    val sizePx = density.run {merged.size.toPx()}
                    val layoutOffset = Offset(
                        offset.first.toFloat(),
                        offset.second.toFloat() - sizePx / 2
                    )
                    fruits.add(FruitMeta("fruit-${fruitCounter++}", merged, layoutOffset))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitObject(
    fruitMeta: FruitMeta,
    onClick: ((String) -> Unit)? = null
) {
    val density = LocalDensity.current
    Box(
        Modifier
            .offset(with(density) {fruitMeta.offset.x.toDp() - (fruitMeta.fruit.size / 2) }, with(density) {fruitMeta.offset.y.toDp() })
    ) {
        Card(
            modifier = Modifier
                .physicsBody(
                    id = fruitMeta.id,
                    shape = CircleShape,
                    dragConfig = DragConfig()
                )
                .size(fruitMeta.fruit.size),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = fruitMeta.fruit.color),
            onClick = { onClick?.invoke(fruitMeta.id) }
        ) {
            Box(
                modifier = Modifier.size(fruitMeta.fruit.size),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = fruitMeta.fruit.displayName.substring(0, 1),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = fruitMeta.fruit.textSize,
                    color = Color.White
                )
            }
        }
    }

}

@Immutable
data class FruitMeta(
    val id: String,
    val fruit: Fruit,
    val offset: Offset
)