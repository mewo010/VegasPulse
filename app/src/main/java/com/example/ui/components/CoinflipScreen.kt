package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinflipBet
import com.example.model.CoinflipChoice
import com.example.model.CoinflipPhase
import com.example.ui.theme.CasinoAmber
import com.example.ui.theme.CasinoBackground
import com.example.ui.theme.CasinoBorder
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoSurface
import com.example.ui.theme.CasinoSurfaceVariant
import com.example.ui.theme.CasinoTextMuted
import com.example.ui.theme.CasinoTextPrimary
import com.example.ui.theme.CasinoTextSecondary
import kotlin.math.abs
import kotlin.math.cos

@Composable
fun CoinflipScreen(
    phase: CoinflipPhase,
    timeLeft: Int,
    result: CoinflipChoice?,
    history: List<CoinflipChoice>,
    bets: List<CoinflipBet>,
    balance: Long,
    onPlaceBet: (choice: CoinflipChoice, amount: Long) -> Boolean
) {
    var selectedAmount by remember { mutableStateOf(100L) }
    val amounts = listOf(50L, 100L, 250L, 500L, 1000L)

    val flipTarget = if (phase == CoinflipPhase.FLIPPING) 10f else 0f
    val flipScale by animateFloatAsState(
        targetValue = flipTarget,
        animationSpec = tween(
            durationMillis = if (phase == CoinflipPhase.FLIPPING) 3000 else 300,
            easing = FastOutSlowInEasing
        ),
        label = "CoinFlipAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status & Round Timer
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
                                    CoinflipPhase.BETTING -> CasinoGreen
                                    CoinflipPhase.FLIPPING -> CasinoAmber
                                    CoinflipPhase.PAYOUT -> CasinoGold
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (phase) {
                            CoinflipPhase.BETTING -> "LIVE COINFLIP DUEL"
                            CoinflipPhase.FLIPPING -> "COIN IN AIR..."
                            CoinflipPhase.PAYOUT -> "RESULT: ${result?.name}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CasinoGold
                    )
                }

                Text(
                    text = "${timeLeft}s",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recent Flips History
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Streak:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoTextMuted,
                modifier = Modifier.padding(end = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                history.take(6).forEach { choice ->
                    val isHeads = choice == CoinflipChoice.HEADS
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isHeads) CasinoAmber else Color(0xFF3B82F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHeads) "H" else "T",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3D Coin Canvas
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF261852), CasinoBackground))),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                // Simulated 3D tilt
                val cosVal = abs(cos(flipScale.toDouble() * Math.PI)).toFloat().coerceAtLeast(0.08f)
                val isShowingHeads = if (phase == CoinflipPhase.PAYOUT) {
                    result == CoinflipChoice.HEADS
                } else {
                    ((flipScale * 2).toInt() % 2 == 0)
                }

                scale(scaleX = cosVal, scaleY = 1f, pivot = center) {
                    val coinBrush = if (isShowingHeads) {
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFC59B27)),
                            center = center,
                            radius = radius
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFF93C5FD), Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                            center = center,
                            radius = radius
                        )
                    }

                    drawCircle(brush = coinBrush, radius = radius)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = radius * 0.85f,
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Text on coin
            val currentDisplayedChoice = if (phase == CoinflipPhase.PAYOUT && result != null) {
                result
            } else {
                if (((flipScale * 2).toInt() % 2 == 0)) CoinflipChoice.HEADS else CoinflipChoice.TAILS
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (currentDisplayedChoice == CoinflipChoice.HEADS) "👑" else "⚔️",
                    fontSize = 32.sp
                )
                Text(
                    text = currentDisplayedChoice.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Payout Multiplier Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CasinoSurfaceVariant)
                .border(1.dp, CasinoBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⚡ WIN MULTIPLIER: 1.96X",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bet Amount Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            amounts.forEach { amt ->
                val isSelected = selectedAmount == amt
                Box(
                    modifier = Modifier
                        .size(48.dp, 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CasinoGold else CasinoSurfaceVariant)
                        .border(1.dp, if (isSelected) Color.White else CasinoBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedAmount = amt },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$amt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.Black else CasinoTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Cards (Heads vs Tails)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bet HEADS
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = phase == CoinflipPhase.BETTING) {
                        onPlaceBet(CoinflipChoice.HEADS, selectedAmount)
                    },
                colors = CardDefaults.cardColors(containerColor = CasinoSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoAmber)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👑", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "HEADS", fontWeight = FontWeight.Black, color = CasinoGold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${bets.count { it.choice == CoinflipChoice.HEADS }} Bets",
                        fontSize = 11.sp,
                        color = CasinoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onPlaceBet(CoinflipChoice.HEADS, selectedAmount) },
                        enabled = phase == CoinflipPhase.BETTING,
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "BET $selectedAmount", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            // Bet TAILS
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = phase == CoinflipPhase.BETTING) {
                        onPlaceBet(CoinflipChoice.TAILS, selectedAmount)
                    },
                colors = CardDefaults.cardColors(containerColor = CasinoSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "⚔️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "TAILS", fontWeight = FontWeight.Black, color = Color(0xFF60A5FA), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${bets.count { it.choice == CoinflipChoice.TAILS }} Bets",
                        fontSize = 11.sp,
                        color = CasinoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onPlaceBet(CoinflipChoice.TAILS, selectedAmount) },
                        enabled = phase == CoinflipPhase.BETTING,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "BET $selectedAmount", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
