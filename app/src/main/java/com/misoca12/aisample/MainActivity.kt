package com.misoca12.aisample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World
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
        val simulation = rememberSimulation()

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

//            Column(
//                Modifier
//                    .fillMaxSize()
//                    .padding(bottom = 32.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Bottom)
//            ) {
//                StarLauncher(
//                    color = blue,
//                ) {
//                    stars.add(StarMeta("star-${starCounter++}", blue))
//                }
//            }
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
                    id = id,
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
