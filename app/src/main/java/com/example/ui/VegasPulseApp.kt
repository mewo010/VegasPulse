package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CoinflipScreen
import com.example.ui.components.DailyRewardDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.LeaderboardScreen
import com.example.ui.components.PrivateDuelScreen
import com.example.ui.components.RouletteScreen
import com.example.ui.components.SlotsScreen
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
import com.example.viewmodel.CasinoViewModel

@Composable
fun VegasPulseApp(viewModel: CasinoViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf("roulette") }
    var showDailyModal by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }

    val balance by viewModel.balance.collectAsState()
    val streakDay by viewModel.streakDay.collectAsState()
    val lastClaimTime by viewModel.lastClaimTime.collectAsState()
    val announcement by viewModel.announcement.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val duelFlipAngle by viewModel.duelFlipAngle.collectAsState()

    // Roulette States
    val roulettePhase by viewModel.roulettePhase.collectAsState()
    val rouletteTimeLeft by viewModel.rouletteTimeLeft.collectAsState()
    val rouletteAngle by viewModel.rouletteTargetAngle.collectAsState()
    val rouletteWinner by viewModel.rouletteWinningPocket.collectAsState()
    val rouletteHistory by viewModel.rouletteHistory.collectAsState()
    val rouletteBets by viewModel.rouletteBets.collectAsState()

    // Coinflip States
    val coinflipPhase by viewModel.coinflipPhase.collectAsState()
    val coinflipTimeLeft by viewModel.coinflipTimeLeft.collectAsState()
    val coinflipResult by viewModel.coinflipResult.collectAsState()
    val coinflipHistory by viewModel.coinflipHistory.collectAsState()
    val coinflipBets by viewModel.coinflipBets.collectAsState()

    // Slots States
    val slotReels by viewModel.slotReels.collectAsState()
    val isSlotSpinning by viewModel.isSlotSpinning.collectAsState()
    val lastSlotWin by viewModel.lastSlotWin.collectAsState()
    val isSlotJackpot by viewModel.isSlotJackpot.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CasinoBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CasinoSurface)
                    .statusBarsPadding()
                    .border(width = 0.5.dp, color = CasinoBorder)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Real Player Profile Badge (Clickable to Edit)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showProfileModal = true }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("top_user_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CasinoSurfaceVariant)
                                .border(1.5.dp, CasinoGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userAvatar, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✏️",
                                    fontSize = 10.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CasinoGreen)
                                    )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeRoom != null) "Room #${activeRoom?.code}" else "Online",
                                    fontSize = 10.sp,
                                    color = if (activeRoom != null) CasinoAmber else CasinoTextSecondary,
                                    fontWeight = if (activeRoom != null) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Top Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Daily Claim Button
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = CasinoAmber,
                                    contentColor = Color.Black
                                ) {
                                    Text("D$streakDay", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CasinoSurfaceVariant)
                                    .border(1.dp, CasinoBorder, RoundedCornerShape(10.dp))
                                    .clickable { showDailyModal = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Redeem,
                                    contentDescription = "Daily Reward",
                                    tint = CasinoGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Virtual Coins Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CasinoSurfaceVariant)
                                .border(1.dp, CasinoGold, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%,d", balance),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CasinoGold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CasinoSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == "roulette",
                    onClick = { currentTab = "roulette" },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Roulette") },
                    label = { Text("Roulette", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CasinoGold,
                        selectedTextColor = CasinoGold,
                        unselectedIconColor = CasinoTextMuted,
                        unselectedTextColor = CasinoTextMuted,
                        indicatorColor = CasinoSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "coinflip",
                    onClick = { currentTab = "coinflip" },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Coinflip") },
                    label = { Text("Coinflip", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CasinoGold,
                        selectedTextColor = CasinoGold,
                        unselectedIconColor = CasinoTextMuted,
                        unselectedTextColor = CasinoTextMuted,
                        indicatorColor = CasinoSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "duel",
                    onClick = { currentTab = "duel" },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeRoom != null) {
                                    Badge(
                                        containerColor = CasinoAmber,
                                        contentColor = Color.Black
                                    ) {
                                        Text("1", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = "1v1 Duel")
                        }
                    },
                    label = { Text("1v1 Duel", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CasinoGold,
                        selectedTextColor = CasinoGold,
                        unselectedIconColor = CasinoTextMuted,
                        unselectedTextColor = CasinoTextMuted,
                        indicatorColor = CasinoSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "slots",
                    onClick = { currentTab = "slots" },
                    icon = { Icon(Icons.Default.Casino, contentDescription = "Slots") },
                    label = { Text("Slots", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CasinoGold,
                        selectedTextColor = CasinoGold,
                        unselectedIconColor = CasinoTextMuted,
                        unselectedTextColor = CasinoTextMuted,
                        indicatorColor = CasinoSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "leaderboard",
                    onClick = { currentTab = "leaderboard" },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Rankings") },
                    label = { Text("Rankings", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CasinoGold,
                        selectedTextColor = CasinoGold,
                        unselectedIconColor = CasinoTextMuted,
                        unselectedTextColor = CasinoTextMuted,
                        indicatorColor = CasinoSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "roulette" -> RouletteScreen(
                    phase = roulettePhase,
                    timeLeft = rouletteTimeLeft,
                    targetAngle = rouletteAngle,
                    winningPocket = rouletteWinner,
                    history = rouletteHistory,
                    bets = rouletteBets,
                    balance = balance,
                    onPlaceBet = { type, value, amount ->
                        viewModel.placeRouletteBet(type, value, amount)
                    }
                )
                "coinflip" -> CoinflipScreen(
                    phase = coinflipPhase,
                    timeLeft = coinflipTimeLeft,
                    result = coinflipResult,
                    history = coinflipHistory,
                    bets = coinflipBets,
                    balance = balance,
                    onPlaceBet = { choice, amount ->
                        viewModel.placeCoinflipBet(choice, amount)
                    }
                )
                "duel" -> PrivateDuelScreen(
                    room = activeRoom,
                    balance = balance,
                    currentUserId = viewModel.userId,
                    flipAngle = duelFlipAngle,
                    onCreateRoom = { stake -> viewModel.createPrivateRoom(stake) },
                    onJoinRoom = { code -> viewModel.joinPrivateRoom(code) },
                    onSelectChoice = { choice -> viewModel.selectDuelChoice(choice) },
                    onStartMatch = { viewModel.startDuelMatch() },
                    onRematch = { viewModel.rematchDuel() },
                    onLeaveRoom = { viewModel.leaveDuelRoom() },
                    onAddTestOpponent = { viewModel.addTestOpponent() }
                )
                "slots" -> SlotsScreen(
                    reels = slotReels,
                    isSpinning = isSlotSpinning,
                    lastWin = lastSlotWin,
                    isJackpot = isSlotJackpot,
                    balance = balance,
                    onSpin = { bet -> viewModel.spinSlots(bet) }
                )
                "leaderboard" -> LeaderboardScreen(
                    balanceList = viewModel.getLeaderboardByBalance(),
                    biggestWinList = viewModel.getLeaderboardByBiggestWin(),
                    streakList = viewModel.getLeaderboardByStreak(),
                    duelsList = viewModel.getLeaderboardByDuels(),
                    onEditProfile = { showProfileModal = true }
                )
            }

            // Real-time Announcement / Payout Floating Toast
            AnimatedVisibility(
                visible = announcement != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 14.dp, end = 14.dp)
            ) {
                announcement?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CasinoAmber)
                            .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                            .clickable { viewModel.dismissAnnouncement() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Daily 24h Streak Claim Modal
            if (showDailyModal) {
                DailyRewardDialog(
                    streakDay = streakDay,
                    lastClaimTime = lastClaimTime,
                    rewards = viewModel.streakRewards,
                    onDismiss = { showDailyModal = false },
                    onClaim = { viewModel.claimDailyReward() }
                )
            }

            // VIP Player Profile Customization Modal
            if (showProfileModal) {
                EditProfileDialog(
                    currentName = userName,
                    currentAvatar = userAvatar,
                    onDismiss = { showProfileModal = false },
                    onSave = { newName, newAvatar ->
                        viewModel.updateProfile(newName, newAvatar)
                        showProfileModal = false
                    }
                )
            }
        }
    }
}
