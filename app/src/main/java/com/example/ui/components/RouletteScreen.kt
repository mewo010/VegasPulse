package com.example.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CasinoBet
import com.example.model.RoulettePhase
import com.example.model.RoulettePocket
import com.example.ui.theme.CasinoAmber
import com.example.ui.theme.CasinoBackground
import com.example.ui.theme.CasinoBorder
import com.example.ui.theme.CasinoDarkRed
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoRed
import com.example.ui.theme.CasinoSurface
import com.example.ui.theme.CasinoSurfaceVariant
import com.example.ui.theme.CasinoTextMuted
import com.example.ui.theme.CasinoTextPrimary
import com.example.ui.theme.CasinoTextSecondary
import com.example.ui.theme.ChipBlue
import com.example.ui.theme.ChipGold
import com.example.ui.theme.ChipPurple
import com.example.ui.theme.ChipRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteScreen(
    phase: RoulettePhase,
    timeLeft: Int,
    targetAngle: Float,
    winningPocket: RoulettePocket?,
    history: List<RoulettePocket>,
    bets: List<CasinoBet>,
    balance: Long,
    onPlaceBet: (type: String, value: String, amount: Long) -> Boolean
) {
    var selectedChip by remember { mutableStateOf(100L) }
    val chipValues = listOf(25L, 50L, 100L, 250L, 500L, 1000L)

    val animatedRotation by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(
            durationMillis = if (phase == RoulettePhase.SPINNING) 5000 else 400,
            easing = LinearOutSlowInEasing
        ),
        label = "RouletteRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PHASE & TIMER BANNER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (phase) {
                                    RoulettePhase.BETTING -> CasinoGreen
                                    RoulettePhase.SPINNING -> CasinoAmber
                                    RoulettePhase.PAYOUT -> CasinoRed
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (phase) {
                            RoulettePhase.BETTING -> "BETTING OPEN"
                            RoulettePhase.SPINNING -> "WHEEL SPINNING"
                            RoulettePhase.PAYOUT -> "PAYOUT REVEAL"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (phase) {
                            RoulettePhase.BETTING -> CasinoGreen
                            RoulettePhase.SPINNING -> CasinoAmber
                            RoulettePhase.PAYOUT -> CasinoGold
                        }
                    )
                }

                Text(
                    text = when (phase) {
                        RoulettePhase.BETTING -> "Closes in ${timeLeft}s"
                        RoulettePhase.SPINNING -> "Resolving in ${timeLeft}s"
                        RoulettePhase.PAYOUT -> "Winner: #${winningPocket?.number ?: "--"}"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CasinoTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- RECENT HISTORY STRIP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoTextMuted,
                modifier = Modifier.padding(end = 8.dp)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                history.take(7).forEach { pocket ->
                    val bgColor = when (pocket.color) {
                        "green" -> CasinoGreen
                        "red" -> CasinoRed
                        else -> Color(0xFF1E1838)
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(1.dp, CasinoBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pocket.number.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ANIMATED ROULETTE WHEEL CANVAS ---
        Box(
            modifier = Modifier
                .size(210.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF24154A), CasinoBackground)))
                .border(3.dp, CasinoGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                // Outer wood/brass ring
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF38235E), Color(0xFF130926)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )

                // Rotating pockets
                rotate(degrees = animatedRotation, pivot = center) {
                    val pockets = 37
                    val sweepAngle = 360f / pockets

                    for (i in 0 until pockets) {
                        val pocketColor = when {
                            i == 0 -> Color(0xFF00C853) // Green 0
                            i % 2 == 0 -> Color(0xFFE53935) // Red
                            else -> Color(0xFF121212) // Black
                        }
                        drawArc(
                            color = pocketColor,
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset.Zero
                        )
                    }

                    // Inner pocket divider ring
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = radius * 0.75f,
                        style = Stroke(width = 2f)
                    )
                }

                // Center brass turret hub
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFEE58), Color(0xFFC59B27), Color(0xFF6B4A0E)),
                        center = center,
                        radius = radius * 0.35f
                    ),
                    radius = radius * 0.35f
                )

                // Turret spokes
                for (angle in listOf(0f, 90f, 180f, 270f)) {
                    val rad = Math.toRadians(angle.toDouble())
                    val spokeEnd = Offset(
                        (center.x + (radius * 0.32f) * cos(rad)).toFloat(),
                        (center.y + (radius * 0.32f) * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFFFFF9C4),
                        start = center,
                        end = spokeEnd,
                        strokeWidth = 3f
                    )
                }
            }

            // Top indicator needle
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(14.dp, 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CasinoAmber)
                    .border(1.dp, Color.White, RoundedCornerShape(3.dp))
            )

            // Winning number popup overlay during payout
            if (phase == RoulettePhase.PAYOUT && winningPocket != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            when (winningPocket.color) {
                                "green" -> CasinoGreen
                                "red" -> CasinoRed
                                else -> Color(0xFF1E1838)
                            }
                        )
                        .border(2.dp, CasinoGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = winningPocket.number.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = winningPocket.color.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CasinoGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- CHIP SELECTOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center
        ) {
            chipValues.forEach { chip ->
                val isSelected = selectedChip == chip
                val chipColor = when (chip) {
                    25L -> ChipBlue
                    50L -> ChipPurple
                    100L -> ChipRed
                    250L -> CasinoAmber
                    500L -> CasinoGreen
                    else -> ChipGold
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(chipColor)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0x66FFFFFF),
                            shape = CircleShape
                        )
                        .clickable { selectedChip = chip },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$chip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (chip >= 500L) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- INTERACTIVE BETTING BOARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "PLACE YOUR BETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CasinoAmber,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Single Zero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CasinoGreen)
                        .clickable(enabled = phase == RoulettePhase.BETTING) {
                            onPlaceBet("NUMBER", "0", selectedChip)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "0  (PAYS 35:1)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Outside Bets (Row 1: Red / Black)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoRed)
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("RED", "RED", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "RED (1:1)", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF181528))
                            .border(1.dp, CasinoBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("BLACK", "BLACK", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "BLACK (1:1)", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Outside Bets (Row 2: Even / Odd)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoSurfaceVariant)
                            .border(1.dp, CasinoBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("EVEN", "EVEN", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "EVEN", fontWeight = FontWeight.Bold, color = CasinoTextPrimary, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoSurfaceVariant)
                            .border(1.dp, CasinoBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("ODD", "ODD", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "ODD", fontWeight = FontWeight.Bold, color = CasinoTextPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Outside Bets (Row 3: 1-18 / 19-36)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoSurfaceVariant)
                            .border(1.dp, CasinoBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("1-18", "1-18", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "LOW 1-18", fontWeight = FontWeight.Bold, color = CasinoTextSecondary, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoSurfaceVariant)
                            .border(1.dp, CasinoBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == RoulettePhase.BETTING) {
                                onPlaceBet("19-36", "19-36", selectedChip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "HIGH 19-36", fontWeight = FontWeight.Bold, color = CasinoTextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Inside Numbers Quick Row (7, 11, 17, 21, 23, 33)
                Text(
                    text = "QUICK STRAIGHT-UP NUMBERS (35:1)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CasinoTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(7, 11, 17, 21, 23, 33).forEach { num ->
                        val isRed = listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(num)
                        Box(
                            modifier = Modifier
                                .size(44.dp, 36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isRed) CasinoRed else Color(0xFF1A1333))
                                .border(1.dp, CasinoBorder, RoundedCornerShape(6.dp))
                                .clickable(enabled = phase == RoulettePhase.BETTING) {
                                    onPlaceBet("NUMBER", num.toString(), selectedChip)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = num.toString(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- LIVE TABLE ACTIVITY FEED (Bots & Players) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ LIVE ROOM ACTIVITY (${bets.size} BETS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CasinoAmber
                    )
                    Text(
                        text = "Real-time Bot & Player Bets",
                        fontSize = 10.sp,
                        color = CasinoTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (bets.isEmpty()) {
                    Text(
                        text = "Waiting for bets in current round...",
                        fontSize = 11.sp,
                        color = CasinoTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    bets.takeLast(4).reversed().forEach { bet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (bet.isBot) "🤖 " else "👤 ",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = bet.userName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (bet.isBot) CasinoTextSecondary else CasinoGold
                                )
                            }
                            Text(
                                text = "${bet.amount} on ${bet.type} ${if (bet.value != bet.type) "(${bet.value})" else ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CasinoGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
