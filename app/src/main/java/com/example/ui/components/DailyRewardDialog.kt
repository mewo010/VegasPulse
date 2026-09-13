package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CasinoAmber
import com.example.ui.theme.CasinoBackground
import com.example.ui.theme.CasinoBorder
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoSurface
import com.example.ui.theme.CasinoSurfaceVariant
import com.example.ui.theme.CasinoTextPrimary
import com.example.ui.theme.CasinoTextSecondary
import kotlinx.coroutines.delay

@Composable
fun DailyRewardDialog(
    streakDay: Int,
    lastClaimTime: Long,
    rewards: List<Long>,
    onDismiss: () -> Unit,
    onClaim: () -> Boolean
) {
    var timeLeftText by remember { mutableStateOf("") }
    var canClaim by remember { mutableStateOf(false) }

    LaunchedEffect(lastClaimTime) {
        while (true) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            val nextClaim = lastClaimTime + oneDayMs

            if (lastClaimTime == 0L || now >= nextClaim) {
                canClaim = true
                timeLeftText = "READY TO CLAIM!"
            } else {
                canClaim = false
                val diff = nextClaim - now
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (diff % (1000 * 60)) / 1000
                timeLeftText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
            delay(1000L)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CasinoAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Gift",
                                tint = CasinoGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Daily Streak Bonus",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CasinoGold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CasinoTextSecondary)
                    }
                }

                Text(
                    text = "Claim 100% free virtual coins every 24 hours to multiply your rewards!",
                    fontSize = 12.sp,
                    color = CasinoTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // 7-Day Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Days 1 to 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 1..4) {
                            StreakCard(
                                day = d,
                                amount = rewards.getOrElse(d - 1) { 500L },
                                isCurrent = d == streakDay,
                                isCompleted = d < streakDay,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Days 5 to 7
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 5..7) {
                            StreakCard(
                                day = d,
                                amount = rewards.getOrElse(d - 1) { 5000L },
                                isCurrent = d == streakDay,
                                isCompleted = d < streakDay,
                                isJackpot = d == 7,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Timer display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CasinoBackground)
                        .border(1.dp, CasinoBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (canClaim) "STATUS" else "NEXT CLAIM UNLOCKS IN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CasinoTextSecondary
                        )
                        Text(
                            text = timeLeftText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (canClaim) CasinoGreen else CasinoAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Claim Button
                Button(
                    onClick = {
                        if (onClaim()) {
                            onDismiss()
                        }
                    },
                    enabled = canClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CasinoGold,
                        disabledContainerColor = CasinoSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (canClaim) "CLAIM DAY $streakDay COINS" else "COME BACK LATER",
                        color = if (canClaim) Color.Black else CasinoTextSecondary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakCard(
    day: Int,
    amount: Long,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isJackpot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isCurrent -> CasinoGold
        isCompleted -> CasinoGreen
        else -> CasinoBorder
    }

    val backgroundBrush = when {
        isCurrent -> Brush.verticalGradient(listOf(CasinoSurfaceVariant, Color(0xFF38236B)))
        isJackpot -> Brush.verticalGradient(listOf(Color(0xFF3B1F5E), Color(0xFF200F38)))
        else -> Brush.verticalGradient(listOf(CasinoSurfaceVariant, CasinoBackground))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Day $day",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) CasinoGold else CasinoTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "+${amount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isJackpot) CasinoAmber else CasinoTextPrimary
            )
            if (isJackpot) {
                Text(
                    text = "👑 CHEST",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoAmber
                )
            }
            if (isCompleted) {
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Claimed",
                    tint = CasinoGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
