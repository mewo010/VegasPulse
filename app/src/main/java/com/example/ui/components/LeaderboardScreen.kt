package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeaderboardItem
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

@Composable
fun LeaderboardScreen(
    balanceList: List<LeaderboardItem>,
    biggestWinList: List<LeaderboardItem>,
    streakList: List<LeaderboardItem>,
    duelsList: List<LeaderboardItem>,
    onEditProfile: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("balance") } // "balance" | "win" | "streak" | "duels"

    val currentList = when (selectedCategory) {
        "balance" -> balanceList
        "win" -> biggestWinList
        "streak" -> streakList
        else -> duelsList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title & Live Real-Time Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 GLOBAL LEADERBOARDS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoGold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CasinoGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Real Live Sync • Verified Records",
                        fontSize = 10.sp,
                        color = CasinoGreen
                    )
                }
            }

            // Edit Profile Button
            OutlinedButton(
                onClick = onEditProfile,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("open_edit_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = CasinoGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "EDIT PROFILE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoGold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Tabs (2 rows of 2 for clean mobile sizing)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CasinoSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterTab(
                title = "💰 Balance",
                isSelected = selectedCategory == "balance",
                modifier = Modifier.weight(1f),
                onClick = { selectedCategory = "balance" }
            )
            FilterTab(
                title = "⚡ Best Win",
                isSelected = selectedCategory == "win",
                modifier = Modifier.weight(1f),
                onClick = { selectedCategory = "win" }
            )
            FilterTab(
                title = "🔥 Streaks",
                isSelected = selectedCategory == "streak",
                modifier = Modifier.weight(1f),
                onClick = { selectedCategory = "streak" }
            )
            FilterTab(
                title = "⚔️ Duels",
                isSelected = selectedCategory == "duels",
                modifier = Modifier.weight(1f),
                onClick = { selectedCategory = "duels" }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Leaderboard List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentList) { item ->
                val isTop3 = item.rank <= 3
                val medal = when (item.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#${item.rank}"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            item.name.contains("(You)") -> Color(0xFF2E1C5E)
                            item.isRealPlayer -> Color(0xFF1E1738)
                            else -> CasinoSurface
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (item.name.contains("(You)")) 1.5.dp else if (item.rank == 1) 1.dp else 0.5.dp,
                        color = when {
                            item.name.contains("(You)") -> CasinoGold
                            item.rank == 1 -> CasinoAmber
                            else -> CasinoBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = medal,
                                fontSize = if (isTop3) 16.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CasinoTextPrimary,
                                modifier = Modifier.width(30.dp)
                            )

                            Text(
                                text = item.avatar,
                                fontSize = 22.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.name.contains("(You)")) CasinoGold else Color.White
                                    )
                                    if (item.isRealPlayer) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 5.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CasinoGreen.copy(alpha = 0.2f))
                                                .border(0.5.dp, CasinoGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = CasinoGreen,
                                                    modifier = Modifier.size(8.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "REAL",
                                                    fontSize = 8.sp,
                                                    color = CasinoGreen,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 5.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CasinoSurfaceVariant)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "BOT",
                                                fontSize = 8.sp,
                                                color = CasinoTextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (item.isRealPlayer) "Verified Player" else "Automated VIP",
                                    fontSize = 10.sp,
                                    color = CasinoTextMuted
                                )
                            }
                        }

                        Text(
                            text = item.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CasinoGreen
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun FilterTab(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CasinoGold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.Black else CasinoTextSecondary,
            maxLines = 1
        )
    }
}
