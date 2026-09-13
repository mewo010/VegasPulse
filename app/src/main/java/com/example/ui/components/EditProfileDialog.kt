package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.ui.theme.CasinoTextMuted
import com.example.ui.theme.CasinoTextPrimary
import com.example.ui.theme.CasinoTextSecondary

private val AVAILABLE_AVATARS = listOf(
    "👑", "🦁", "🐯", "💎",
    "🐉", "🦈", "🚀", "🤠",
    "🎲", "🎰", "🐍", "⭐"
)

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: String,
    onDismiss: () -> Unit,
    onSave: (name: String, avatar: String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CUSTOMIZE PROFILE",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = CasinoGold
                        )
                        Text(
                            text = "Real identity on global leaderboards & 1v1 duels",
                            fontSize = 11.sp,
                            color = CasinoTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dismiss_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CasinoTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Avatar Preview
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(CasinoSurfaceVariant)
                        .border(2.dp, CasinoGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedAvatar,
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Username Input
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { if (it.length <= 16) nameText = it },
                    label = { Text("Player Display Name", color = CasinoTextMuted) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = CasinoGold
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CasinoGold,
                        unfocusedBorderColor = CasinoBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = CasinoGold,
                        focusedContainerColor = CasinoBackground,
                        unfocusedContainerColor = CasinoBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (nameText.isNotBlank()) onSave(nameText, selectedAvatar)
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Grid
                Text(
                    text = "SELECT VIP AVATAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CasinoTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AVAILABLE_AVATARS) { avatar ->
                        val isSelected = avatar == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CasinoAmber.copy(alpha = 0.25f) else CasinoSurfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CasinoGold else CasinoBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedAvatar = avatar },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatar, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Action Button
                Button(
                    onClick = {
                        val finalName = if (nameText.isBlank()) "HighRoller" else nameText.trim()
                        onSave(finalName, selectedAvatar)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE & SYNC PROFILE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
