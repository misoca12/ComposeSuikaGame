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

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                fruits.add(FruitMeta("fruit-${fruitCounter++}", Fruit.random(), it))
            }
        },
        color = MaterialTheme.colorScheme.background
    ) {
        val simulation = rememberSimulation(onCollision = { id1: String, id2: String ->
                Log.d("MainActivity", "onCollision $id1 and $id2")
                val fruit1 = fruits.firstOrNull { it.id == id1 } ?: return@rememberSimulation
                val fruit2 = fruits.firstOrNull { it.id == id2 } ?: return@rememberSimulation
                if (fruit1.fruit == fruit2.fruit) {
                    fruits.removeIf { it.id == id1 }
                    fruits.removeIf { it.id == id2 }
                }
            })

        GravitySensor { (x, y) ->
            simulation.setGravity(Offset(-x, y).times(3f))
        }

        PhysicsLayout(
            modifier = Modifier.systemBarsPadding(),
            simulation = simulation,
//            shape = RoundedCornerShape(64.dp)
        ) {
            fruits.forEach { fruitMeta ->
                key(fruitMeta.id) {
                    FruitObject(
                        fruitMeta
                    ) { id ->
                        fruits.removeIf { it.id == id }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitObject(
    fruitMeta: FruitMeta,
    onClick: (String) -> Unit
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
            onClick = { onClick(fruitMeta.id) }
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