package com.misoca12.aisample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.misoca12.aisample.ui.theme.AISampleTheme
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
            AISampleTheme {
                GameScreen()
            }
        }
    }
}

@Composable
fun GameScreen() {
    var starCounter by remember { mutableStateOf(0) }
    val stars = remember { mutableStateListOf<StarMeta>() }

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                stars.add(StarMeta("star-${starCounter++}", blue, it))
            }
        },
        color = MaterialTheme.colorScheme.background
    ) {
        val simulation = rememberSimulation(onCollision = { id1: String, id2: String ->
                Log.d("MainActivity", "onCollision $id1 and $id2")
            })

        GravitySensor { (x, y) ->
            simulation.setGravity(Offset(-x, y).times(3f))
        }

        PhysicsLayout(
            modifier = Modifier.systemBarsPadding(),
            simulation = simulation,
            shape = RoundedCornerShape(64.dp)
        ) {
            stars.forEach { starMeta ->
                key(starMeta.id) {
                    Star(
                        id = starMeta.id,
                        color = starMeta.color,
                        offset = starMeta.offset
                    ) { id ->
                        stars.removeIf { it.id == id }
                    }
                }
            }
        }

    }
}

@Composable
fun StarLauncher(
    color: Color,
    onStar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .physicsBody(
                shape = CircleShape,
                bodyConfig = BodyConfig(isStatic = true)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val job = scope.launch {
                            while (true) {
                                onStar()
                                delay(100)
                            }
                        }
                        tryAwaitRelease()
                        job.cancel()
                    }
                )
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                imageVector = Icons.Default.Add,
                contentDescription = "Add red"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.Star(
    id: String,
    color: Color,
    offset: Offset,
    onClick: (String) -> Unit
) {
    val density = LocalDensity.current
    Box(
        Modifier
//            .align(Alignment.TopCenter)
            .offset(with(density) {offset.x.toDp() }, with(density) {offset.y.toDp() })
            .padding(top = 32.dp)
    ) {
        Card(
            modifier = Modifier
                .physicsBody(
                    shape = CircleShape,
                    dragConfig = DragConfig()
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = color),
            onClick = { onClick(id) }
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                imageVector = Icons.Default.Star,
                contentDescription = "",
                tint = Color.White
            )
        }
    }

}

@Immutable
data class StarMeta(
    val id: String,
    val color: Color,
    val offset: Offset
)

private val blue = Color(0xFF42A5F5)
