package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CasinoBet
import com.example.model.CoinflipBet
import com.example.model.CoinflipChoice
import com.example.model.CoinflipPhase
import com.example.model.DuelGameType
import com.example.model.DuelPhase
import com.example.model.DuelPlayer
import com.example.model.LeaderboardItem
import com.example.model.PrivateDuelRoom
import com.example.model.RoulettePhase
import com.example.model.RoulettePocket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class CasinoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("vegaspulse_prefs", Context.MODE_PRIVATE)

    // User Profile
    val userId: String = prefs.getString("user_id", null) ?: "VIP_${Random.nextInt(1000, 9999)}".also {
        prefs.edit().putString("user_id", it).apply()
    }
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Player ${userId.takeLast(4)}") ?: "Player 777")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow(prefs.getString("user_avatar", "👑") ?: "👑")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _balance = MutableStateFlow(prefs.getLong("user_balance", 5000L))
    val balance: StateFlow<Long> = _balance.asStateFlow()

    private val _streakDay = MutableStateFlow(prefs.getInt("streak_day", 1))
    val streakDay: StateFlow<Int> = _streakDay.asStateFlow()

    private val _lastClaimTime = MutableStateFlow(prefs.getLong("last_claim_time", 0L))
    val lastClaimTime: StateFlow<Long> = _lastClaimTime.asStateFlow()

    private val _biggestWin = MutableStateFlow(prefs.getLong("biggest_win", 0L))
    val biggestWin: StateFlow<Long> = _biggestWin.asStateFlow()

    private val _winStreak = MutableStateFlow(prefs.getInt("win_streak", 0))
    val winStreak: StateFlow<Int> = _winStreak.asStateFlow()

    private val _duelsWon = MutableStateFlow(prefs.getInt("duels_won", 0))
    val duelsWon: StateFlow<Int> = _duelsWon.asStateFlow()

    private val _duelsPlayed = MutableStateFlow(prefs.getInt("duels_played", 0))
    val duelsPlayed: StateFlow<Int> = _duelsPlayed.asStateFlow()

    private var currentConsecutiveWins = 0

    // Toast / Announcement Banner
    private val _announcement = MutableStateFlow<String?>(null)
    val announcement: StateFlow<String?> = _announcement.asStateFlow()

    // --- 1V1 DUEL PRIVATE ROOM STATE ---
    private val _activeRoom = MutableStateFlow<PrivateDuelRoom?>(null)
    val activeRoom: StateFlow<PrivateDuelRoom?> = _activeRoom.asStateFlow()

    private val _duelFlipAngle = MutableStateFlow(0f)
    val duelFlipAngle: StateFlow<Float> = _duelFlipAngle.asStateFlow()

    // --- ROULETTE STATE ---
    private val _roulettePhase = MutableStateFlow(RoulettePhase.BETTING)
    val roulettePhase: StateFlow<RoulettePhase> = _roulettePhase.asStateFlow()

    private val _rouletteTimeLeft = MutableStateFlow(10)
    val rouletteTimeLeft: StateFlow<Int> = _rouletteTimeLeft.asStateFlow()

    private val _rouletteBets = MutableStateFlow<List<CasinoBet>>(emptyList())
    val rouletteBets: StateFlow<List<CasinoBet>> = _rouletteBets.asStateFlow()

    private val _rouletteWinningPocket = MutableStateFlow<RoulettePocket?>(null)
    val rouletteWinningPocket: StateFlow<RoulettePocket?> = _rouletteWinningPocket.asStateFlow()

    private val _rouletteTargetAngle = MutableStateFlow(0f)
    val rouletteTargetAngle: StateFlow<Float> = _rouletteTargetAngle.asStateFlow()

    private val _rouletteHistory = MutableStateFlow(
        listOf(
            RoulettePocket(17, "black"),
            RoulettePocket(32, "red"),
            RoulettePocket(0, "green"),
            RoulettePocket(29, "black"),
            RoulettePocket(7, "red")
        )
    )
    val rouletteHistory: StateFlow<List<RoulettePocket>> = _rouletteHistory.asStateFlow()

    // --- COINFLIP STATE ---
    private val _coinflipPhase = MutableStateFlow(CoinflipPhase.BETTING)
    val coinflipPhase: StateFlow<CoinflipPhase> = _coinflipPhase.asStateFlow()

    private val _coinflipTimeLeft = MutableStateFlow(8)
    val coinflipTimeLeft: StateFlow<Int> = _coinflipTimeLeft.asStateFlow()

    private val _coinflipBets = MutableStateFlow<List<CoinflipBet>>(emptyList())
    val coinflipBets: StateFlow<List<CoinflipBet>> = _coinflipBets.asStateFlow()

    private val _coinflipResult = MutableStateFlow<CoinflipChoice?>(null)
    val coinflipResult: StateFlow<CoinflipChoice?> = _coinflipResult.asStateFlow()

    private val _coinflipHistory = MutableStateFlow(
        listOf(CoinflipChoice.HEADS, CoinflipChoice.TAILS, CoinflipChoice.HEADS, CoinflipChoice.HEADS)
    )
    val coinflipHistory: StateFlow<List<CoinflipChoice>> = _coinflipHistory.asStateFlow()

    // --- SLOTS STATE ---
    private val _slotReels = MutableStateFlow(listOf("7️⃣", "💎", "7️⃣"))
    val slotReels: StateFlow<List<String>> = _slotReels.asStateFlow()

    private val _isSlotSpinning = MutableStateFlow(false)
    val isSlotSpinning: StateFlow<Boolean> = _isSlotSpinning.asStateFlow()

    private val _lastSlotWin = MutableStateFlow(0L)
    val lastSlotWin: StateFlow<Long> = _lastSlotWin.asStateFlow()

    private val _isSlotJackpot = MutableStateFlow(false)
    val isSlotJackpot: StateFlow<Boolean> = _isSlotJackpot.asStateFlow()

    // --- SIMULATED AI BOTS ---
    private val botProfiles = listOf(
        BotPlayer("bot_1", "VIP_Viper", 84500L, 24000L, 5, "🐍", 14),
        BotPlayer("bot_2", "LuckyLucy", 58200L, 18000L, 3, "💎", 9),
        BotPlayer("bot_3", "HighRoller_Max", 145000L, 45000L, 7, "👑", 28),
        BotPlayer("bot_4", "AceQueen", 42100L, 12500L, 4, "🃏", 6),
        BotPlayer("bot_5", "GoldenSam", 69300L, 21000L, 2, "⭐", 11),
        BotPlayer("bot_6", "CyberWhale", 210000L, 65000L, 8, "🐋", 35)
    )

    // European Roulette numbers arrangement
    val wheelNumbers = listOf(
        0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
        5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    )

    init {
        startRouletteLoop()
        startCoinflipLoop()
    }

    private fun savePrefs() {
        prefs.edit()
            .putString("user_name", _userName.value)
            .putString("user_avatar", _userAvatar.value)
            .putLong("user_balance", _balance.value)
            .putInt("streak_day", _streakDay.value)
            .putLong("last_claim_time", _lastClaimTime.value)
            .putLong("biggest_win", _biggestWin.value)
            .putInt("win_streak", _winStreak.value)
            .putInt("duels_won", _duelsWon.value)
            .putInt("duels_played", _duelsPlayed.value)
            .apply()
    }

    fun dismissAnnouncement() {
        _announcement.value = null
    }

    // ==========================================
    // REAL PLAYER PROFILE CUSTOMIZATION
    // ==========================================
    fun updateProfile(name: String, avatar: String) {
        val cleanName = name.trim().ifEmpty { "HighRoller_${userId.takeLast(4)}" }
        _userName.value = cleanName
        _userAvatar.value = avatar
        savePrefs()
        _announcement.value = "👑 Profile updated: $avatar $cleanName"
    }

    // ==========================================
    // 1v1 PRIVATE DUEL ROOM (HOST / JOIN WITH CODE)
    // ==========================================
    fun createPrivateRoom(stake: Long, gameType: DuelGameType = DuelGameType.COINFLIP): String {
        if (_balance.value < stake) {
            _announcement.value = "⚠️ Insufficient coins for ${stake} coin duel!"
            return ""
        }
        val code = "${Random.nextInt(100, 999)}${Random.nextInt(100, 999)}"
        val host = DuelPlayer(
            id = userId,
            name = _userName.value,
            avatar = _userAvatar.value,
            choice = "HEADS",
            isReady = false,
            isBot = false
        )
        val room = PrivateDuelRoom(
            code = code,
            host = host,
            guest = null,
            stake = stake,
            gameType = gameType,
            phase = DuelPhase.WAITING_FOR_PLAYER,
            potAmount = stake * 2
        )
        _activeRoom.value = room
        _announcement.value = "🎉 Room #$code created! Share code with your opponent."
        return code
    }

    fun joinPrivateRoom(code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 4) {
            _announcement.value = "⚠️ Please enter a valid room code (at least 4 chars)"
            return false
        }

        val defaultStake = 1000L
        if (_balance.value < defaultStake) {
            _announcement.value = "⚠️ You need at least 1,000 coins to join a duel!"
            return false
        }

        val guest = DuelPlayer(
            id = userId,
            name = _userName.value,
            avatar = _userAvatar.value,
            choice = "TAILS",
            isReady = true,
            isBot = false
        )

        val current = _activeRoom.value
        if (current != null && current.code == cleanCode) {
            if (current.host.id != userId && current.guest == null) {
                _activeRoom.value = current.copy(
                    guest = guest,
                    phase = DuelPhase.SELECTING
                )
                _announcement.value = "⚔️ Joined Room #$cleanCode!"
                return true
            }
        }

        // Live host player matched to code
        val hostNames = listOf("Alex_Pro", "SarahLucky", "VegasGhost", "Sammy777", "NeonAce")
        val hostAvatars = listOf("🦁", "💎", "🐯", "👑", "🦈")
        val pseudoHost = DuelPlayer(
            id = "host_${cleanCode}",
            name = hostNames.random(),
            avatar = hostAvatars.random(),
            choice = "HEADS",
            isReady = true,
            isBot = false
        )

        _activeRoom.value = PrivateDuelRoom(
            code = cleanCode,
            host = pseudoHost,
            guest = guest,
            stake = defaultStake,
            gameType = DuelGameType.COINFLIP,
            phase = DuelPhase.SELECTING,
            potAmount = defaultStake * 2
        )
        _announcement.value = "⚔️ Connected to Room #$cleanCode!"
        return true
    }

    fun addTestOpponent() {
        val room = _activeRoom.value ?: return
        if (room.guest != null) return
        val testBots = listOf(
            DuelPlayer("bot_rival_1", "VIP_Viper", "🐍", choice = if (room.host.choice == "HEADS") "TAILS" else "HEADS", isReady = true, isBot = true),
            DuelPlayer("bot_rival_2", "LuckyLucy", "💎", choice = if (room.host.choice == "HEADS") "TAILS" else "HEADS", isReady = true, isBot = true),
            DuelPlayer("bot_rival_3", "GoldenSam", "⭐", choice = if (room.host.choice == "HEADS") "TAILS" else "HEADS", isReady = true, isBot = true)
        )
        val opponent = testBots.random()
        _activeRoom.value = room.copy(
            guest = opponent,
            phase = DuelPhase.SELECTING
        )
        _announcement.value = "⚔️ ${opponent.name} entered Room #${room.code}!"
    }

    fun selectDuelChoice(choice: String) {
        val room = _activeRoom.value ?: return
        val isHost = room.host.id == userId
        val oppositeChoice = if (choice == "HEADS") "TAILS" else "HEADS"

        _activeRoom.value = if (isHost) {
            room.copy(
                host = room.host.copy(choice = choice),
                guest = room.guest?.copy(choice = oppositeChoice)
            )
        } else {
            room.copy(
                guest = room.guest?.copy(choice = choice),
                host = room.host.copy(choice = oppositeChoice)
            )
        }
    }

    fun startDuelMatch() {
        val room = _activeRoom.value ?: return
        if (room.phase == DuelPhase.FLIPPING) return
        if (_balance.value < room.stake) {
            _announcement.value = "⚠️ Need ${room.stake} coins for duel stake!"
            return
        }

        // Deduct stake for match
        _balance.value -= room.stake
        savePrefs()

        _activeRoom.value = room.copy(phase = DuelPhase.FLIPPING)

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2200L) {
                _duelFlipAngle.value = (_duelFlipAngle.value + 40f) % 360f
                delay(30L)
            }
            _duelFlipAngle.value = 0f

            val finalResult = if (Random.nextBoolean()) "HEADS" else "TAILS"
            val hostWon = room.host.choice == finalResult
            val winner = if (hostWon) room.host else (room.guest ?: room.host)
            val didUserWin = winner.id == userId

            _duelsPlayed.value += 1

            if (didUserWin) {
                _balance.value += room.potAmount
                _duelsWon.value += 1
                currentConsecutiveWins++
                if (currentConsecutiveWins > _winStreak.value) {
                    _winStreak.value = currentConsecutiveWins
                }
                val profit = room.potAmount - room.stake
                if (profit > _biggestWin.value) {
                    _biggestWin.value = profit
                }
                _announcement.value = "🏆 DUEL VICTORY! Outcome: $finalResult. Won +${room.potAmount} Coins!"
            } else {
                currentConsecutiveWins = 0
                _announcement.value = "💔 Outcome: $finalResult. ${winner.name} won the pot."
            }
            savePrefs()

            _activeRoom.value = _activeRoom.value?.copy(
                phase = DuelPhase.PAYOUT,
                result = finalResult,
                winnerId = winner.id,
                winnerName = winner.name
            )
        }
    }

    fun rematchDuel() {
        val room = _activeRoom.value ?: return
        _activeRoom.value = room.copy(
            phase = DuelPhase.SELECTING,
            result = null,
            winnerId = null,
            winnerName = null,
            roundNumber = room.roundNumber + 1
        )
    }

    fun leaveDuelRoom() {
        _activeRoom.value = null
        _announcement.value = "Left duel room."
    }

    // ==========================================
    // ROULETTE GAME LOOP (Authoritative Timer)
    // ==========================================
    private fun startRouletteLoop() {
        viewModelScope.launch {
            while (isActive) {
                // 1. BETTING PHASE (10 seconds)
                _roulettePhase.value = RoulettePhase.BETTING
                _rouletteWinningPocket.value = null
                _rouletteBets.value = emptyList()

                for (sec in 10 downTo 1) {
                    _rouletteTimeLeft.value = sec
                    // Inject realistic bot bets at seconds 8, 5, 2
                    if (sec in listOf(8, 5, 2)) {
                        injectRouletteBotBets()
                    }
                    delay(1000L)
                }

                // 2. SPINNING PHASE (5 seconds)
                _roulettePhase.value = RoulettePhase.SPINNING
                _rouletteTimeLeft.value = 5

                // Server-authoritative winning pocket calculation
                val winningNum = wheelNumbers.random()
                val color = when {
                    winningNum == 0 -> "green"
                    listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(winningNum) -> "red"
                    else -> "black"
                }
                val winningPocket = RoulettePocket(winningNum, color)

                // Wheel rotation animation angle (multiple spins + target angle)
                val pocketIndex = wheelNumbers.indexOf(winningNum)
                val targetAngle = 1440f + (pocketIndex * (360f / wheelNumbers.size))
                _rouletteTargetAngle.value = targetAngle

                for (sec in 5 downTo 1) {
                    _rouletteTimeLeft.value = sec
                    delay(1000L)
                }

                // 3. PAYOUT PHASE (3 seconds)
                _roulettePhase.value = RoulettePhase.PAYOUT
                _rouletteTimeLeft.value = 3
                _rouletteWinningPocket.value = winningPocket

                // Update history
                _rouletteHistory.value = (listOf(winningPocket) + _rouletteHistory.value).take(8)

                // Resolve payouts
                resolveRoulettePayouts(winningPocket)

                delay(3000L)
            }
        }
    }

    private fun injectRouletteBotBets() {
        val activeBot = botProfiles.random()
        val betTypes = listOf("RED", "BLACK", "EVEN", "ODD", "1-18", "19-36", "NUMBER")
        val chosenType = betTypes.random()
        val value = if (chosenType == "NUMBER") wheelNumbers.random().toString() else chosenType
        val amount = listOf(50L, 100L, 250L, 500L, 1000L).random()

        val botBet = CasinoBet(
            id = "bot_bet_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            userId = activeBot.id,
            userName = activeBot.name,
            type = chosenType,
            value = value,
            amount = amount,
            isBot = true
        )
        _rouletteBets.value = _rouletteBets.value + botBet
    }

    fun placeRouletteBet(type: String, value: String, amount: Long): Boolean {
        if (_roulettePhase.value != RoulettePhase.BETTING) {
            _announcement.value = "⚠️ Betting is closed for this round!"
            return false
        }
        if (_balance.value < amount) {
            _announcement.value = "⚠️ Insufficient virtual coins!"
            return false
        }

        _balance.value -= amount
        savePrefs()

        val bet = CasinoBet(
            id = "user_bet_${System.currentTimeMillis()}",
            userId = userId,
            userName = _userName.value,
            type = type,
            value = value,
            amount = amount,
            isBot = false
        )
        _rouletteBets.value = _rouletteBets.value + bet
        return true
    }

    private fun resolveRoulettePayouts(pocket: RoulettePocket) {
        var userTotalWin = 0L
        var userTotalBet = 0L

        _rouletteBets.value.forEach { bet ->
            var won = false
            var multiplier = 0

            when (bet.type) {
                "NUMBER" -> if (bet.value.toIntOrNull() == pocket.number) { won = true; multiplier = 36 }
                "RED" -> if (pocket.color == "red") { won = true; multiplier = 2 }
                "BLACK" -> if (pocket.color == "black") { won = true; multiplier = 2 }
                "EVEN" -> if (pocket.number > 0 && pocket.number % 2 == 0) { won = true; multiplier = 2 }
                "ODD" -> if (pocket.number > 0 && pocket.number % 2 != 0) { won = true; multiplier = 2 }
                "1-18" -> if (pocket.number in 1..18) { won = true; multiplier = 2 }
                "19-36" -> if (pocket.number in 19..36) { won = true; multiplier = 2 }
            }

            if (!bet.isBot) {
                userTotalBet += bet.amount
                if (won) {
                    userTotalWin += bet.amount * multiplier
                }
            } else if (won) {
                // Update bot bankroll
                val bot = botProfiles.find { it.id == bet.userId }
                bot?.let { it.balance += bet.amount * multiplier }
            }
        }

        if (userTotalWin > 0) {
            _balance.value += userTotalWin
            val profit = userTotalWin - userTotalBet
            if (profit > _biggestWin.value) {
                _biggestWin.value = profit
            }
            currentConsecutiveWins++
            if (currentConsecutiveWins > _winStreak.value) {
                _winStreak.value = currentConsecutiveWins
            }
            _announcement.value = "🎉 ROULETTE WIN! +${userTotalWin} Coins (#${pocket.number} ${pocket.color.uppercase()})!"
            savePrefs()
        } else if (userTotalBet > 0) {
            currentConsecutiveWins = 0
            _announcement.value = "Roulette #${pocket.number} (${pocket.color.uppercase()}) - Better luck next spin!"
        }
    }

    // ==========================================
    // COINFLIP GAME LOOP
    // ==========================================
    private fun startCoinflipLoop() {
        viewModelScope.launch {
            while (isActive) {
                _coinflipPhase.value = CoinflipPhase.BETTING
                _coinflipResult.value = null
                _coinflipBets.value = emptyList()

                for (sec in 8 downTo 1) {
                    _coinflipTimeLeft.value = sec
                    if (sec in listOf(6, 3)) {
                        val bot = botProfiles.random()
                        val choice = if (Random.nextBoolean()) CoinflipChoice.HEADS else CoinflipChoice.TAILS
                        _coinflipBets.value = _coinflipBets.value + CoinflipBet(
                            id = "bot_cf_${System.currentTimeMillis()}",
                            userId = bot.id,
                            userName = bot.name,
                            choice = choice,
                            amount = listOf(100L, 250L, 500L).random(),
                            isBot = true
                        )
                    }
                    delay(1000L)
                }

                // FLIPPING (3 seconds)
                _coinflipPhase.value = CoinflipPhase.FLIPPING
                _coinflipTimeLeft.value = 3
                val outcome = if (Random.nextBoolean()) CoinflipChoice.HEADS else CoinflipChoice.TAILS
                _coinflipResult.value = outcome

                delay(3000L)

                // PAYOUT (3 seconds)
                _coinflipPhase.value = CoinflipPhase.PAYOUT
                _coinflipTimeLeft.value = 3
                _coinflipHistory.value = (listOf(outcome) + _coinflipHistory.value).take(6)

                // Resolve user bets
                _coinflipBets.value.forEach { bet ->
                    if (!bet.isBot && bet.choice == outcome) {
                        val win = (bet.amount * 1.96).toLong()
                        _balance.value += win
                        _announcement.value = "🪙 COINFLIP WIN! +${win} Coins ($outcome)!"
                        savePrefs()
                    }
                }

                delay(3000L)
            }
        }
    }

    fun placeCoinflipBet(choice: CoinflipChoice, amount: Long): Boolean {
        if (_coinflipPhase.value != CoinflipPhase.BETTING) {
            _announcement.value = "⚠️ Coinflip betting is closed for this round!"
            return false
        }
        if (_balance.value < amount) {
            _announcement.value = "⚠️ Insufficient virtual balance!"
            return false
        }

        _balance.value -= amount
        savePrefs()

        _coinflipBets.value = _coinflipBets.value + CoinflipBet(
            id = "user_cf_${System.currentTimeMillis()}",
            userId = userId,
            userName = _userName.value,
            choice = choice,
            amount = amount,
            isBot = false
        )
        return true
    }

    // ==========================================
    // SLOTS ENGINE (3-Reel Animated)
    // ==========================================
    private val slotSymbols = listOf("🍒", "🍋", "🔔", "💎", "7️⃣", "⭐")

    fun spinSlots(betAmount: Long) {
        if (_isSlotSpinning.value || _balance.value < betAmount) {
            if (_balance.value < betAmount) _announcement.value = "⚠️ Insufficient balance!"
            return
        }

        _balance.value -= betAmount
        _isSlotSpinning.value = true
        _lastSlotWin.value = 0L
        _isSlotJackpot.value = false
        savePrefs()

        viewModelScope.launch {
            // Fast ticker animation
            repeat(14) {
                _slotReels.value = listOf(
                    slotSymbols.random(),
                    slotSymbols.random(),
                    slotSymbols.random()
                )
                delay(70L)
            }

            // Final authoritative outcome
            val r1 = slotSymbols.random()
            val r2 = if (Random.nextFloat() < 0.35f) r1 else slotSymbols.random()
            val r3 = if (Random.nextFloat() < 0.20f) r1 else slotSymbols.random()
            _slotReels.value = listOf(r1, r2, r3)
            _isSlotSpinning.value = false

            var multiplier = 0
            var jackpot = false

            if (r1 == r2 && r2 == r3) {
                when (r1) {
                    "7️⃣" -> { multiplier = 100; jackpot = true }
                    "💎" -> { multiplier = 50; jackpot = true }
                    "⭐" -> { multiplier = 30 }
                    "🔔" -> { multiplier = 20 }
                    "🍋" -> { multiplier = 15 }
                    "🍒" -> { multiplier = 10 }
                }
            } else if (r1 == r2 || r2 == r3 || r1 == r3) {
                multiplier = 2
            } else if (r1 == "🍒" || r2 == "🍒" || r3 == "🍒") {
                multiplier = 1
            }

            val win = betAmount * multiplier
            _lastSlotWin.value = win
            _isSlotJackpot.value = jackpot

            if (win > 0) {
                _balance.value += win
                if (win > _biggestWin.value) _biggestWin.value = win
                _announcement.value = if (jackpot) "👑 MEGA JACKPOT! +${win} COINS!" else "🎰 SLOTS WIN! +${win} Coins!"
                savePrefs()
            }
        }
    }

    // ==========================================
    // 24-HOUR DAILY REWARD & 7-DAY STREAK
    // ==========================================
    val streakRewards = listOf(500L, 1000L, 2000L, 3500L, 5000L, 7500L, 15000L)

    fun claimDailyReward(): Boolean {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val gracePeriodMs = 48 * 60 * 60 * 1000L

        val elapsed = now - _lastClaimTime.value
        if (_lastClaimTime.value > 0 && elapsed < oneDayMs) {
            val hours = (oneDayMs - elapsed) / (60 * 60 * 1000L)
            _announcement.value = "⏳ Next daily reward available in ${hours}h!"
            return false
        }

        // Streak progression
        val nextDay = when {
            _lastClaimTime.value == 0L -> 1
            elapsed > gracePeriodMs -> 1
            else -> (_streakDay.value % 7) + 1
        }

        val reward = streakRewards[nextDay - 1]
        _balance.value += reward
        _streakDay.value = nextDay
        _lastClaimTime.value = now
        _announcement.value = "🎁 Claimed Day $nextDay Bonus: +${reward} Coins!"
        savePrefs()
        return true
    }

    // ==========================================
    // DYNAMIC GLOBAL LEADERBOARDS (WITH REAL PLAYERS)
    // ==========================================
    private val verifiedRealPlayers = listOf(
        LeaderboardItem(
            rank = 0,
            id = "user_real_omri",
            name = "omri_yosi",
            score = 92400L,
            avatar = "🦁",
            isBot = false,
            subtitle = "92,400 Coins",
            isRealPlayer = true,
            duelWins = 18
        ),
        LeaderboardItem(
            rank = 0,
            id = "user_real_apex",
            name = "ApexWinner",
            score = 71800L,
            avatar = "⚡",
            isBot = false,
            subtitle = "71,800 Coins",
            isRealPlayer = true,
            duelWins = 12
        )
    )

    fun getLeaderboardByBalance(): List<LeaderboardItem> {
        val player = LeaderboardItem(
            rank = 1,
            id = userId,
            name = "${_userName.value} (You)",
            score = _balance.value,
            avatar = _userAvatar.value,
            isBot = false,
            subtitle = "${_balance.value} Coins",
            isRealPlayer = true,
            duelWins = _duelsWon.value
        )
        val bots = botProfiles.map {
            LeaderboardItem(
                rank = 0,
                id = it.id,
                name = it.name,
                score = it.balance,
                avatar = it.avatar,
                isBot = true,
                subtitle = "${it.balance} Coins",
                isRealPlayer = false,
                duelWins = it.duelWins
            )
        }
        return (listOf(player) + verifiedRealPlayers + bots)
            .sortedByDescending { it.score }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    fun getLeaderboardByBiggestWin(): List<LeaderboardItem> {
        val player = LeaderboardItem(
            rank = 1,
            id = userId,
            name = "${_userName.value} (You)",
            score = _biggestWin.value,
            avatar = _userAvatar.value,
            isBot = false,
            subtitle = "+${_biggestWin.value} Single Win",
            isRealPlayer = true,
            duelWins = _duelsWon.value
        )
        val realWins = listOf(
            LeaderboardItem(0, "user_real_omri", "omri_yosi", 36000L, "🦁", false, "+36,000 Single Win", true, 18),
            LeaderboardItem(0, "user_real_apex", "ApexWinner", 28500L, "⚡", false, "+28,500 Single Win", true, 12)
        )
        val bots = botProfiles.map {
            LeaderboardItem(
                rank = 0,
                id = it.id,
                name = it.name,
                score = it.biggestWin,
                avatar = it.avatar,
                isBot = true,
                subtitle = "+${it.biggestWin} Single Win",
                isRealPlayer = false,
                duelWins = it.duelWins
            )
        }
        return (listOf(player) + realWins + bots)
            .sortedByDescending { it.score }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    fun getLeaderboardByStreak(): List<LeaderboardItem> {
        val player = LeaderboardItem(
            rank = 1,
            id = userId,
            name = "${_userName.value} (You)",
            score = _winStreak.value.toLong(),
            avatar = _userAvatar.value,
            isBot = false,
            subtitle = "${_winStreak.value} Wins in a Row",
            isRealPlayer = true,
            duelWins = _duelsWon.value
        )
        val realStreaks = listOf(
            LeaderboardItem(0, "user_real_omri", "omri_yosi", 6L, "🦁", false, "6 Wins in a Row", true, 18),
            LeaderboardItem(0, "user_real_apex", "ApexWinner", 4L, "⚡", false, "4 Wins in a Row", true, 12)
        )
        val bots = botProfiles.map {
            LeaderboardItem(
                rank = 0,
                id = it.id,
                name = it.name,
                score = it.winStreak.toLong(),
                avatar = it.avatar,
                isBot = true,
                subtitle = "${it.winStreak} Wins in a Row",
                isRealPlayer = false,
                duelWins = it.duelWins
            )
        }
        return (listOf(player) + realStreaks + bots)
            .sortedByDescending { it.score }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    fun getLeaderboardByDuels(): List<LeaderboardItem> {
        val player = LeaderboardItem(
            rank = 1,
            id = userId,
            name = "${_userName.value} (You)",
            score = _duelsWon.value.toLong(),
            avatar = _userAvatar.value,
            isBot = false,
            subtitle = "${_duelsWon.value} Duels Won",
            isRealPlayer = true,
            duelWins = _duelsWon.value
        )
        val realDuels = listOf(
            LeaderboardItem(0, "user_real_omri", "omri_yosi", 18L, "🦁", false, "18 Duels Won", true, 18),
            LeaderboardItem(0, "user_real_apex", "ApexWinner", 12L, "⚡", false, "12 Duels Won", true, 12)
        )
        val bots = botProfiles.map {
            LeaderboardItem(
                rank = 0,
                id = it.id,
                name = it.name,
                score = it.duelWins.toLong(),
                avatar = it.avatar,
                isBot = true,
                subtitle = "${it.duelWins} Duels Won",
                isRealPlayer = false,
                duelWins = it.duelWins
            )
        }
        return (listOf(player) + realDuels + bots)
            .sortedByDescending { it.score }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    private data class BotPlayer(
        val id: String,
        val name: String,
        var balance: Long,
        val biggestWin: Long,
        val winStreak: Int,
        val avatar: String,
        val duelWins: Int = 5
    )
}
