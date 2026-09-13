package com.example.model

enum class RoulettePhase {
    BETTING,
    SPINNING,
    PAYOUT
}

data class RoulettePocket(
    val number: Int,
    val color: String // "red", "black", "green"
)

data class CasinoBet(
    val id: String,
    val userId: String,
    val userName: String,
    val type: String, // "RED", "BLACK", "EVEN", "ODD", "1-18", "19-36", "NUMBER"
    val value: String,
    val amount: Long,
    val isBot: Boolean = false
)

enum class CoinflipChoice {
    HEADS,
    TAILS
}

enum class CoinflipPhase {
    BETTING,
    FLIPPING,
    PAYOUT
}

data class CoinflipBet(
    val id: String,
    val userId: String,
    val userName: String,
    val choice: CoinflipChoice,
    val amount: Long,
    val isBot: Boolean = false
)

data class LeaderboardItem(
    val rank: Int,
    val id: String,
    val name: String,
    val score: Long,
    val avatar: String,
    val isBot: Boolean,
    val subtitle: String,
    val isRealPlayer: Boolean = !isBot,
    val duelWins: Int = 0
)

enum class DuelPhase {
    WAITING_FOR_PLAYER,
    SELECTING,
    FLIPPING,
    PAYOUT
}

enum class DuelGameType {
    COINFLIP,
    DICE_ROLL
}

data class DuelPlayer(
    val id: String,
    val name: String,
    val avatar: String,
    val choice: String? = null, // "HEADS", "TAILS", or "ROLL"
    val rollValue: Int = 0,
    val isReady: Boolean = false,
    val isBot: Boolean = false
)

data class PrivateDuelRoom(
    val code: String,
    val host: DuelPlayer,
    val guest: DuelPlayer? = null,
    val stake: Long = 1000L,
    val gameType: DuelGameType = DuelGameType.COINFLIP,
    val phase: DuelPhase = DuelPhase.WAITING_FOR_PLAYER,
    val result: String? = null, // "HEADS", "TAILS", or "6"
    val winnerId: String? = null,
    val winnerName: String? = null,
    val potAmount: Long = 2000L,
    val roundNumber: Int = 1
)
