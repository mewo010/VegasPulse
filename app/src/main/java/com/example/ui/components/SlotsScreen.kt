package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CasinoAmber
import com.example.ui.theme.CasinoBackground
import com.example.ui.theme.CasinoBorder
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoRed
import com.example.ui.theme.CasinoSurface
import com.example.ui.theme.CasinoSurfaceVariant
import com.example.ui.theme.CasinoTextMuted
import com.example.ui.theme.CasinoTextPrimary
import com.example.ui.theme.CasinoTextSecondary

@Composable
fun SlotsScreen(
    reels: List<String>,
    isSpinning: Boolean,
    lastWin: Long,
    isJackpot: Boolean,
    balance: Long,
    onSpin: (bet: Long) -> Unit
) {
    var selectedBet by remember { mutableStateOf(100L) }
    val bets = listOf(50L, 100L, 250L, 500L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Machine Title
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎰 CYBER JACKPOT 777",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoGold
                )
                Text(
                    text = "Instant Spins • 100X Triple Seven Jackpot",
                    fontSize = 11.sp,
                    color = CasinoTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slots Machine Frame with 3 Reels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF28184E), Color(0xFF130926))
                    )
                )
                .border(2.5.dp, if (isJackpot) CasinoGold else Color(0xFF7C3AED), RoundedCornerShape(22.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Reel Window
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    reels.forEachIndexed { index, symbol ->
                        Box(
                            modifier = Modifier
                                .size(88.dp, 105.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF07040F))
                                .border(1.5.dp, CasinoBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = symbol,
                                fontSize = 42.sp
                            )
                        }
                    }
                }

                // Payline Indicator
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CasinoAmber.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "— CENTER PAYLINE ACTIVE —",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CasinoAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Win / Jackpot Announcement Banner
        AnimatedVisibility(
            visible = lastWin > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isJackpot) CasinoAmber else CasinoGreen)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isJackpot) "👑 100X MEGA JACKPOT! +${lastWin} COINS!" else "🎉 YOU WON +${lastWin} VIRTUAL COINS!",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bet Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bets.forEach { b ->
                val isSelected = selectedBet == b
                Box(
                    modifier = Modifier
                        .size(68.dp, 40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CasinoGold else CasinoSurfaceVariant)
                        .border(1.dp, if (isSelected) Color.White else CasinoBorder, RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSpinning) { selectedBet = b },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$b",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.Black else CasinoTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Big Gold Spin Button
        Button(
            onClick = { onSpin(selectedBet) },
            enabled = !isSpinning && balance >= selectedBet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CasinoGold,
                disabledContainerColor = CasinoSurfaceVariant
            )
        ) {
            Text(
                text = if (isSpinning) "SPINNING REELS..." else "SPIN ($selectedBet COINS)",
                color = if (!isSpinning && balance >= selectedBet) Color.Black else CasinoTextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Paytable Reference Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🏆 PAYTABLE MULTIPLIERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CasinoAmber,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "7️⃣ 7️⃣ 7️⃣ (Lucky Sevens)", color = CasinoTextPrimary, fontSize = 12.sp)
                    Text(text = "100X (JACKPOT)", color = CasinoGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "💎 💎 💎 (Diamonds)", color = CasinoTextPrimary, fontSize = 12.sp)
                    Text(text = "50X", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "⭐ ⭐ ⭐ (Stars)", color = CasinoTextPrimary, fontSize = 12.sp)
                    Text(text = "30X", color = CasinoAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Any 2 Matching Symbols", color = CasinoTextSecondary, fontSize = 12.sp)
                    Text(text = "2X", color = CasinoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
