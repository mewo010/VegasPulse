package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DuelPhase
import com.example.model.DuelPlayer
import com.example.model.PrivateDuelRoom
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
fun PrivateDuelScreen(
    room: PrivateDuelRoom?,
    balance: Long,
    currentUserId: String,
    flipAngle: Float,
    onCreateRoom: (stake: Long) -> String,
    onJoinRoom: (code: String) -> Boolean,
    onSelectChoice: (choice: String) -> Unit,
    onStartMatch: () -> Unit,
    onRematch: () -> Unit,
    onLeaveRoom: () -> Unit,
    onAddTestOpponent: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeTab by remember { mutableStateOf("create") } // "create" | "join"
    var selectedStake by remember { mutableLongStateOf(1000L) }
    var inputCode by remember { mutableStateOf("") }
    var copiedFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (room == null) {
            // ==========================================
            // NO ACTIVE ROOM: CREATE OR JOIN LOBBY
            // ==========================================
            Text(
                text = "⚔️ 1v1 PRIVATE DUEL",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = CasinoGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Open a private room with a code & battle friends live",
                fontSize = 11.sp,
                color = CasinoTextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CasinoSurfaceVariant)
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "➕ CREATE ROOM",
                    isSelected = activeTab == "create",
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = "create" }
                )
                TabButton(
                    title = "🔑 ENTER CODE",
                    isSelected = activeTab == "join",
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = "join" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == "create") {
                // CREATE ROOM PANEL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CasinoSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SELECT DUEL STAKE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CasinoGold
                        )
                        Text(
                            text = "Both players wager this amount. Winner takes entire pot!",
                            fontSize = 10.sp,
                            color = CasinoTextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Stake chips
                        val stakes = listOf(250L, 500L, 1000L, 2500L, 5000L, 10000L)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stakes.take(3).forEach { stake ->
                                StakeChip(
                                    amount = stake,
                                    isSelected = selectedStake == stake,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedStake = stake }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stakes.takeLast(3).forEach { stake ->
                                StakeChip(
                                    amount = stake,
                                    isSelected = selectedStake == stake,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedStake = stake }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pot preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CasinoSurfaceVariant)
                                .border(1.dp, CasinoGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Total Duel Pot: ${String.format("%,d", selectedStake * 2)} Coins",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CasinoGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onCreateRoom(selectedStake) },
                            enabled = balance >= selectedStake,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("create_duel_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "OPEN ROOM & GET CODE",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // JOIN WITH CODE PANEL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CasinoSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENTER 6-DIGIT ROOM CODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CasinoGold
                        )
                        Text(
                            text = "Ask the room host for their code to enter the duel",
                            fontSize = 10.sp,
                            color = CasinoTextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { if (it.length <= 8) inputCode = it.uppercase() },
                            placeholder = { Text("e.g. 742918", color = CasinoTextMuted) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                                        inputCode = pasteText.trim().uppercase()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = CasinoGold
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CasinoGold,
                                unfocusedBorderColor = CasinoBorder,
                                focusedContainerColor = CasinoBackground,
                                unfocusedContainerColor = CasinoBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_code_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (inputCode.isNotBlank()) onJoinRoom(inputCode)
                            },
                            enabled = inputCode.length >= 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("join_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ENTER DUEL ARENA",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // How Code Multiplayer Works Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, CasinoBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 HOW PLAYING WITH FRIENDS WORKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CasinoGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Player A taps 'CREATE ROOM', chooses stakes & gets a unique code.",
                        fontSize = 10.sp,
                        color = CasinoTextSecondary
                    )
                    Text(
                        text = "2. Player B enters the code under 'ENTER CODE' on their device.",
                        fontSize = 10.sp,
                        color = CasinoTextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "3. Both lock in Heads or Tails -> Live synchronized 3D flip -> Winner gets the pot!",
                        fontSize = 10.sp,
                        color = CasinoTextSecondary
                    )
                }
            }
        } else {
            // ==========================================
            // ACTIVE ROOM VIEW (LOBBY OR DUEL ARENA)
            // ==========================================
            ActiveRoomView(
                room = room,
                currentUserId = currentUserId,
                flipAngle = flipAngle,
                balance = balance,
                copiedFeedback = copiedFeedback,
                onCopyCode = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Room Code", room.code)
                    clipboard.setPrimaryClip(clip)
                    copiedFeedback = true
                },
                onSelectChoice = onSelectChoice,
                onStartMatch = onStartMatch,
                onRematch = onRematch,
                onLeaveRoom = onLeaveRoom,
                onAddTestOpponent = onAddTestOpponent
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun ActiveRoomView(
    room: PrivateDuelRoom,
    currentUserId: String,
    flipAngle: Float,
    balance: Long,
    copiedFeedback: Boolean,
    onCopyCode: () -> Unit,
    onSelectChoice: (choice: String) -> Unit,
    onStartMatch: () -> Unit,
    onRematch: () -> Unit,
    onLeaveRoom: () -> Unit,
    onAddTestOpponent: () -> Unit
) {
    val isHost = room.host.id == currentUserId
    val myPlayer = if (isHost) room.host else room.guest
    val otherPlayer = if (isHost) room.guest else room.host

    // Top Navigation & Room Code Bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onLeaveRoom,
            modifier = Modifier.testTag("leave_room_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Leave",
                tint = CasinoTextSecondary
            )
        }

        // Room Code Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CasinoSurfaceVariant)
                .border(1.dp, CasinoGold, RoundedCornerShape(12.dp))
                .clickable { onCopyCode() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("room_code_badge")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CODE: #${room.code}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoGold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = CasinoGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = "Rnd ${room.roundNumber}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CasinoTextMuted
        )
    }

    if (copiedFeedback) {
        Text(
            text = "✓ Room code copied to clipboard!",
            fontSize = 10.sp,
            color = CasinoGreen,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Big Pot Banner
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🪙 1v1 DUEL POT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoTextSecondary
            )
            Text(
                text = "${String.format("%,d", room.potAmount)} COINS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = CasinoGold
            )
            Text(
                text = "Wager: ${String.format("%,d", room.stake)} Coins each",
                fontSize = 11.sp,
                color = CasinoTextMuted
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // VS Arena: Player 1 (Host) vs Player 2 (Guest)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player 1 Card
        PlayerDuelCard(
            player = room.host,
            isMe = room.host.id == currentUserId,
            title = "HOST",
            modifier = Modifier.weight(1f)
        )

        // VS Emblem in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CasinoAmber)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        // Player 2 Card (or waiting)
        if (room.guest != null) {
            PlayerDuelCard(
                player = room.guest,
                isMe = room.guest.id == currentUserId,
                title = "GUEST",
                modifier = Modifier.weight(1f)
            )
        } else {
            // Waiting for Opponent
            WaitingOpponentCard(
                code = room.code,
                onAddTest = onAddTestOpponent,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Game Phase Interactions
    when (room.phase) {
        DuelPhase.WAITING_FOR_PLAYER -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoAmber.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⏳ WAITING FOR OPPONENT...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = CasinoAmber
                    )
                    Text(
                        text = "Share Room Code #${room.code} with your friend to connect.",
                        fontSize = 11.sp,
                        color = CasinoTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = onAddTestOpponent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_opponent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🤖 Play with High Roller Bot (Instant Test)",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        DuelPhase.SELECTING -> {
            // Pick Side and Start
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHOOSE YOUR SIDE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CasinoGold
                    )
                    Text(
                        text = "Opponent automatically takes the other side",
                        fontSize = 10.sp,
                        color = CasinoTextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val myCurrentChoice = myPlayer?.choice ?: "HEADS"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChoiceButton(
                            title = "👑 HEADS",
                            isSelected = myCurrentChoice == "HEADS",
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectChoice("HEADS") }
                        )
                        ChoiceButton(
                            title = "🦅 TAILS",
                            isSelected = myCurrentChoice == "TAILS",
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectChoice("TAILS") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartMatch,
                        enabled = balance >= room.stake,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("flip_duel_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "FLIP FOR THE POT! (🪙 ${room.stake})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        DuelPhase.FLIPPING -> {
            // 3D Coin Flip In Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🪙 COIN IS IN THE AIR...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CasinoGold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            rotationY = flipAngle
                            cameraDistance = 12f * density
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(CasinoGold, CasinoAmber, Color(0xFF6B4B00)))
                        )
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (flipAngle % 360 < 180) "👑" else "🦅",
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "3... 2... 1...",
                    fontSize = 13.sp,
                    color = CasinoTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        DuelPhase.PAYOUT -> {
            // Victory / Outcome Card
            val didIWin = room.winnerId == currentUserId
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (didIWin) Color(0xFF163E24) else Color(0xFF381414)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (didIWin) CasinoGreen else CasinoRed
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (didIWin) "🏆 VICTORY!" else "💔 DEFEAT",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (didIWin) CasinoGold else Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Coin landed on ${room.result}!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (didIWin) {
                            "+${String.format("%,d", room.potAmount)} COINS AWARDED!"
                        } else {
                            "${room.winnerName} took the ${String.format("%,d", room.potAmount)} pot"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (didIWin) CasinoGreen else CasinoTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRematch,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("rematch_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⚔️ REMATCH",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onLeaveRoom,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("exit_duel_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "LEAVE ROOM",
                                color = CasinoTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerDuelCard(
    player: DuelPlayer,
    isMe: Boolean,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFF241C48) else CasinoSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMe) CasinoGold else CasinoBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isMe) "$title (YOU)" else title,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isMe) CasinoGold else CasinoTextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CasinoSurfaceVariant)
                    .border(1.dp, CasinoGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.avatar, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = player.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Choice Badge
            val choiceText = player.choice ?: "WAITING"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (choiceText == "HEADS") CasinoGold else CasinoAmber)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = choiceText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun WaitingOpponentCard(
    code: String,
    onAddTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoBorder.copy(alpha = alpha))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OPPONENT",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CasinoTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CasinoSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "❓", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Waiting...",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoAmber
            )

            Text(
                text = "Enter code #$code",
                fontSize = 9.sp,
                color = CasinoTextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CasinoGold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.Black else CasinoTextSecondary
        )
    }
}

@Composable
private fun StakeChip(
    amount: Long,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CasinoGold else CasinoSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) CasinoGold else CasinoBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%,d", amount),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
private fun ChoiceButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CasinoGold else CasinoSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CasinoGold else CasinoBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}
