/**
 * VegasPulse Casino Server
 * Node.js + Express + Socket.io Server-Authoritative Game Server
 * 
 * Features:
 * - Real-time Room Syncing & Synchronized Mini-Games
 * - Server-Authoritative 10s Betting -> 5s Spin -> 3s Payout Game Loops
 * - Automated AI Bots placing realistic randomized bets
 * - 24-Hour Daily Reward Claim with 7-Day Streak Multipliers
 * - Global Leaderboards (Balance, Biggest Single Win, Win Streaks)
 */

const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3001;

// ==========================================
// 1. IN-MEMORY STATE STORE (Production DB mock)
// ==========================================

const ROULETTE_NUMBERS = [
  { num: 0, color: 'green' },
  { num: 1, color: 'red' }, { num: 2, color: 'black' }, { num: 3, color: 'red' },
  { num: 4, color: 'black' }, { num: 5, color: 'red' }, { num: 6, color: 'black' },
  { num: 7, color: 'red' }, { num: 8, color: 'black' }, { num: 9, color: 'red' },
  { num: 10, color: 'black' }, { num: 11, color: 'black' }, { num: 12, color: 'red' },
  { num: 13, color: 'black' }, { num: 14, color: 'red' }, { num: 15, color: 'black' },
  { num: 16, color: 'red' }, { num: 17, color: 'black' }, { num: 18, color: 'red' },
  { num: 19, color: 'red' }, { num: 20, color: 'black' }, { num: 21, color: 'red' },
  { num: 22, color: 'black' }, { num: 23, color: 'red' }, { num: 24, color: 'black' },
  { num: 25, color: 'red' }, { num: 26, color: 'black' }, { num: 27, color: 'red' },
  { num: 28, color: 'black' }, { num: 29, color: 'black' }, { num: 30, color: 'red' },
  { num: 31, color: 'black' }, { num: 32, color: 'red' }, { num: 33, color: 'black' },
  { num: 34, color: 'red' }, { num: 35, color: 'black' }, { num: 36, color: 'red' }
];

// Active Server AI Bots
const BOTS = [
  { id: 'bot_1', name: 'VIP_Viper', balance: 78500, avatar: '🐍', biggestWin: 24000, winStreak: 5, isBot: true, duelWins: 14 },
  { id: 'bot_2', name: 'LuckyLucy', balance: 52400, avatar: '💎', biggestWin: 18000, winStreak: 3, isBot: true, duelWins: 9 },
  { id: 'bot_3', name: 'HighRoller_Max', balance: 132000, avatar: '👑', biggestWin: 45000, winStreak: 7, isBot: true, duelWins: 28 },
  { id: 'bot_4', name: 'AceQueen', balance: 39100, avatar: '🃏', biggestWin: 12500, winStreak: 4, isBot: true, duelWins: 6 },
  { id: 'bot_5', name: 'GoldenSam', balance: 64200, avatar: '⭐', biggestWin: 21000, winStreak: 2, isBot: true, duelWins: 11 },
  { id: 'bot_6', name: 'CyberWhale', balance: 195000, avatar: '🐋', biggestWin: 65000, winStreak: 8, isBot: true, duelWins: 35 }
];

// In-memory player database
const users = new Map();

// Helper to get or create guest user
function getOrCreateUser(userId, name = 'Guest Player') {
  if (!users.has(userId)) {
    users.set(userId, {
      id: userId,
      name: name,
      balance: 5000, // starting free coins
      streakDay: 1,
      lastClaimTime: 0,
      biggestWin: 0,
      winStreak: 0,
      currentStreak: 0,
      isBot: false,
      avatar: '🎰',
      duelWins: 0,
      duelsPlayed: 0
    });
  }
  return users.get(userId);
}

// Initialize bots into leaderboard
BOTS.forEach(b => users.set(b.id, { ...b, streakDay: 1, lastClaimTime: 0, currentStreak: b.winStreak }));

// Daily claim streak rewards (100% free virtual currency)
const STREAK_REWARDS = [500, 1000, 2000, 3500, 5000, 7500, 15000]; // Day 1 to 7

// ==========================================
// 2. ROOM GAME LOOPS (Server-Authoritative)
// ==========================================

// ROULETTE ROOM STATE
const rouletteRoom = {
  roomId: 'roulette-main',
  phase: 'BETTING', // 'BETTING' (10s) -> 'SPINNING' (5s) -> 'PAYOUT' (3s)
  timeLeft: 10,
  winningNumber: null,
  winningColor: null,
  bets: [], // Array of { userId, userName, type, value, amount, isBot }
  history: [
    { num: 17, color: 'black' },
    { num: 32, color: 'red' },
    { num: 0, color: 'green' },
    { num: 29, color: 'black' },
    { num: 7, color: 'red' }
  ]
};

// COINFLIP ROOM STATE
const coinflipRoom = {
  roomId: 'coinflip-main',
  phase: 'BETTING', // 'BETTING' (8s) -> 'FLIPPING' (3s) -> 'PAYOUT' (3s)
  timeLeft: 8,
  result: null, // 'HEADS' or 'TAILS'
  bets: [], // Array of { userId, userName, choice: 'HEADS' | 'TAILS', amount, isBot }
  history: ['HEADS', 'TAILS', 'HEADS', 'HEADS', 'TAILS']
};

// --- Roulette Bot Bet Simulator ---
function simulateBotBetsRoulette() {
  if (rouletteRoom.phase !== 'BETTING') return;
  // Pick 1-3 bots randomly each cycle
  const activeBots = BOTS.filter(() => Math.random() > 0.4);
  activeBots.forEach(bot => {
    // Avoid spamming if bot already has 2 bets this round
    const existingBets = rouletteRoom.bets.filter(b => b.userId === bot.id);
    if (existingBets.length >= 2) return;

    const betTypes = ['RED', 'BLACK', 'EVEN', 'ODD', '1-18', '19-36', 'NUMBER'];
    const chosenType = betTypes[Math.floor(Math.random() * betTypes.length)];
    let betValue = chosenType;
    if (chosenType === 'NUMBER') {
      betValue = Math.floor(Math.random() * 37);
    }
    const chipAmounts = [50, 100, 250, 500, 1000];
    const amount = chipAmounts[Math.floor(Math.random() * chipAmounts.length)];

    rouletteRoom.bets.push({
      userId: bot.id,
      userName: bot.name,
      type: chosenType,
      value: betValue,
      amount: amount,
      isBot: true
    });
  });

  io.to('roulette-main').emit('roulette_bets_update', rouletteRoom.bets);
}

// --- Calculate Roulette Payouts ---
function resolveRoulettePayouts(winningNum, winningColor) {
  const payouts = [];

  rouletteRoom.bets.forEach(bet => {
    let won = false;
    let multiplier = 0;

    if (bet.type === 'NUMBER' && Number(bet.value) === winningNum) {
      won = true;
      multiplier = 36; // 35:1 profit + original bet
    } else if (bet.type === 'RED' && winningColor === 'red') {
      won = true;
      multiplier = 2;
    } else if (bet.type === 'BLACK' && winningColor === 'black') {
      won = true;
      multiplier = 2;
    } else if (bet.type === 'EVEN' && winningNum > 0 && winningNum % 2 === 0) {
      won = true;
      multiplier = 2;
    } else if (bet.type === 'ODD' && winningNum > 0 && winningNum % 2 !== 0) {
      won = true;
      multiplier = 2;
    } else if (bet.type === '1-18' && winningNum >= 1 && winningNum <= 18) {
      won = true;
      multiplier = 2;
    } else if (bet.type === '19-36' && winningNum >= 19 && winningNum <= 36) {
      won = true;
      multiplier = 2;
    }

    const payoutAmount = won ? bet.amount * multiplier : 0;
    const user = users.get(bet.userId);
    if (user) {
      if (won) {
        user.balance += payoutAmount;
        const profit = payoutAmount - bet.amount;
        if (profit > user.biggestWin) user.biggestWin = profit;
        user.currentStreak += 1;
        if (user.currentStreak > user.winStreak) user.winStreak = user.currentStreak;
      } else {
        user.currentStreak = 0;
      }
    }

    payouts.push({
      userId: bet.userId,
      userName: bet.userName,
      won,
      payout: payoutAmount,
      betAmount: bet.amount,
      type: bet.type,
      value: bet.value,
      isBot: bet.isBot
    });
  });

  return payouts;
}

// --- Master 1-Second Timer Tick for Roulette ---
setInterval(() => {
  rouletteRoom.timeLeft -= 1;

  if (rouletteRoom.phase === 'BETTING') {
    // Random bot bet injection at seconds 8, 5, 2
    if ([8, 5, 2].includes(rouletteRoom.timeLeft)) {
      simulateBotBetsRoulette();
    }

    if (rouletteRoom.timeLeft <= 0) {
      // Transition to SPINNING
      rouletteRoom.phase = 'SPINNING';
      rouletteRoom.timeLeft = 5;

      // Authoritative outcome generation
      const selected = ROULETTE_NUMBERS[Math.floor(Math.random() * ROULETTE_NUMBERS.length)];
      rouletteRoom.winningNumber = selected.num;
      rouletteRoom.winningColor = selected.color;

      io.to('roulette-main').emit('roulette_spin_start', {
        phase: 'SPINNING',
        winningNumber: selected.num,
        winningColor: selected.color,
        durationSeconds: 5
      });
    }
  } else if (rouletteRoom.phase === 'SPINNING') {
    if (rouletteRoom.timeLeft <= 0) {
      // Transition to PAYOUT
      rouletteRoom.phase = 'PAYOUT';
      rouletteRoom.timeLeft = 3;

      const payouts = resolveRoulettePayouts(rouletteRoom.winningNumber, rouletteRoom.winningColor);

      // Add to history
      rouletteRoom.history.unshift({
        num: rouletteRoom.winningNumber,
        color: rouletteRoom.winningColor
      });
      if (rouletteRoom.history.length > 10) rouletteRoom.history.pop();

      io.to('roulette-main').emit('roulette_payout', {
        phase: 'PAYOUT',
        winningNumber: rouletteRoom.winningNumber,
        winningColor: rouletteRoom.winningColor,
        payouts: payouts,
        history: rouletteRoom.history
      });

      // Broadcast global leaderboard update
      broadcastLeaderboard();
    }
  } else if (rouletteRoom.phase === 'PAYOUT') {
    if (rouletteRoom.timeLeft <= 0) {
      // Reset for next betting window
      rouletteRoom.phase = 'BETTING';
      rouletteRoom.timeLeft = 10;
      rouletteRoom.bets = [];
      rouletteRoom.winningNumber = null;
      rouletteRoom.winningColor = null;

      io.to('roulette-main').emit('roulette_new_round', {
        phase: 'BETTING',
        timeLeft: 10,
        history: rouletteRoom.history
      });
    }
  }

  // Periodic heartbeat tick
  io.to('roulette-main').emit('roulette_tick', {
    phase: rouletteRoom.phase,
    timeLeft: rouletteRoom.timeLeft,
    betCount: rouletteRoom.bets.length
  });
}, 1000);

// --- Master 1-Second Timer Tick for Coinflip ---
setInterval(() => {
  coinflipRoom.timeLeft -= 1;

  if (coinflipRoom.phase === 'BETTING') {
    if ([6, 3].includes(coinflipRoom.timeLeft)) {
      // Simulate bot bets on coinflip
      const activeBot = BOTS[Math.floor(Math.random() * BOTS.length)];
      if (!coinflipRoom.bets.some(b => b.userId === activeBot.id)) {
        coinflipRoom.bets.push({
          userId: activeBot.id,
          userName: activeBot.name,
          choice: Math.random() > 0.5 ? 'HEADS' : 'TAILS',
          amount: [100, 250, 500][Math.floor(Math.random() * 3)],
          isBot: true
        });
        io.to('coinflip-main').emit('coinflip_bets_update', coinflipRoom.bets);
      }
    }

    if (coinflipRoom.timeLeft <= 0) {
      coinflipRoom.phase = 'FLIPPING';
      coinflipRoom.timeLeft = 3;
      coinflipRoom.result = Math.random() > 0.5 ? 'HEADS' : 'TAILS';

      io.to('coinflip-main').emit('coinflip_start', {
        phase: 'FLIPPING',
        result: coinflipRoom.result,
        duration: 3
      });
    }
  } else if (coinflipRoom.phase === 'FLIPPING') {
    if (coinflipRoom.timeLeft <= 0) {
      coinflipRoom.phase = 'PAYOUT';
      coinflipRoom.timeLeft = 3;

      const payouts = coinflipRoom.bets.map(bet => {
        const won = bet.choice === coinflipRoom.result;
        const payout = won ? Math.floor(bet.amount * 1.96) : 0;
        const user = users.get(bet.userId);
        if (user && won) {
          user.balance += payout;
          const profit = payout - bet.amount;
          if (profit > user.biggestWin) user.biggestWin = profit;
        }
        return { ...bet, won, payout };
      });

      coinflipRoom.history.unshift(coinflipRoom.result);
      if (coinflipRoom.history.length > 8) coinflipRoom.history.pop();

      io.to('coinflip-main').emit('coinflip_payout', {
        phase: 'PAYOUT',
        result: coinflipRoom.result,
        payouts,
        history: coinflipRoom.history
      });
    }
  } else if (coinflipRoom.phase === 'PAYOUT') {
    if (coinflipRoom.timeLeft <= 0) {
      coinflipRoom.phase = 'BETTING';
      coinflipRoom.timeLeft = 8;
      coinflipRoom.bets = [];
      coinflipRoom.result = null;

      io.to('coinflip-main').emit('coinflip_new_round', {
        phase: 'BETTING',
        timeLeft: 8,
        history: coinflipRoom.history
      });
    }
  }
}, 1000);

// ==========================================
// 3. LEADERBOARD SYSTEM
// ==========================================

function getLeaderboards() {
  const allUsers = Array.from(users.values());

  const byBalance = [...allUsers]
    .sort((a, b) => b.balance - a.balance)
    .slice(0, 10)
    .map((u, i) => ({ rank: i + 1, ...u }));

  const byBiggestWin = [...allUsers]
    .sort((a, b) => b.biggestWin - a.biggestWin)
    .slice(0, 10)
    .map((u, i) => ({ rank: i + 1, ...u }));

  const byWinStreak = [...allUsers]
    .sort((a, b) => b.winStreak - a.winStreak)
    .slice(0, 10)
    .map((u, i) => ({ rank: i + 1, ...u }));

  const byDuels = [...allUsers]
    .sort((a, b) => (b.duelWins || 0) - (a.duelWins || 0))
    .slice(0, 10)
    .map((u, i) => ({ rank: i + 1, ...u }));

  return { byBalance, byBiggestWin, byWinStreak, byDuels, timestamp: Date.now() };
}

function broadcastLeaderboard() {
  io.emit('leaderboard_update', getLeaderboards());
}

// ==========================================
// 4. REST API ENDPOINTS
// ==========================================

app.get('/health', (req, res) => {
  res.json({ status: 'ok', time: new Date().toISOString(), server: 'VegasPulse Casino' });
});

// Fetch Leaderboards
app.get('/api/leaderboard', (req, res) => {
  res.json(getLeaderboards());
});

// Fetch User Profile / Balance
app.get('/api/user/:id', (req, res) => {
  const user = getOrCreateUser(req.params.id, req.query.name);
  res.json(user);
});

// 24-Hour Daily Reward Claim with 7-Day Streak
app.post('/api/user/claim-daily', (req, res) => {
  const { userId } = req.body;
  if (!userId) return res.status(400).json({ error: 'userId is required' });

  const user = getOrCreateUser(userId);
  const now = Date.now();
  const ONE_DAY_MS = 24 * 60 * 60 * 1000;
  const GRACE_PERIOD_MS = 48 * 60 * 60 * 1000; // Reset streak if missed after 48h

  const timeSinceLastClaim = now - user.lastClaimTime;

  if (user.lastClaimTime > 0 && timeSinceLastClaim < ONE_DAY_MS) {
    const remainingMs = ONE_DAY_MS - timeSinceLastClaim;
    return res.status(400).json({
      error: 'Daily reward not ready yet',
      remainingMs,
      nextClaimAt: user.lastClaimTime + ONE_DAY_MS
    });
  }

  // Calculate streak day
  if (user.lastClaimTime > 0 && timeSinceLastClaim > GRACE_PERIOD_MS) {
    user.streakDay = 1; // Streak broken
  } else if (user.lastClaimTime > 0) {
    user.streakDay = (user.streakDay % 7) + 1;
  } else {
    user.streakDay = 1;
  }

  const rewardCoins = STREAK_REWARDS[user.streakDay - 1] || 1000;
  user.balance += rewardCoins;
  user.lastClaimTime = now;

  res.json({
    success: true,
    claimedAmount: rewardCoins,
    streakDay: user.streakDay,
    newBalance: user.balance,
    nextClaimAt: now + ONE_DAY_MS
  });
});

// Slot Machine Instant Spin Resolution (Server-authoritative)
app.post('/api/slots/spin', (req, res) => {
  const { userId, betAmount } = req.body;
  if (!userId || !betAmount || betAmount <= 0) {
    return res.status(400).json({ error: 'Valid userId and betAmount required' });
  }

  const user = getOrCreateUser(userId);
  if (user.balance < betAmount) {
    return res.status(400).json({ error: 'Insufficient virtual balance' });
  }

  user.balance -= betAmount;

  // Symbols and payout multipliers
  const symbols = ['CHERRY', 'LEMON', 'BELL', 'DIAMOND', 'SEVEN', 'STAR'];
  const weights = [35, 25, 18, 12, 6, 4]; // percentage probability

  function getRandomSymbol() {
    const total = weights.reduce((a, b) => a + b, 0);
    let rand = Math.random() * total;
    for (let i = 0; i < symbols.length; i++) {
      if (rand < weights[i]) return symbols[i];
      rand -= weights[i];
    }
    return symbols[0];
  }

  const reel1 = getRandomSymbol();
  const reel2 = getRandomSymbol();
  const reel3 = getRandomSymbol();

  let multiplier = 0;
  let isJackpot = false;

  if (reel1 === reel2 && reel2 === reel3) {
    switch (reel1) {
      case 'SEVEN': multiplier = 100; isJackpot = true; break;
      case 'DIAMOND': multiplier = 50; isJackpot = true; break;
      case 'STAR': multiplier = 30; break;
      case 'BELL': multiplier = 20; break;
      case 'LEMON': multiplier = 15; break;
      case 'CHERRY': multiplier = 10; break;
    }
  } else if (reel1 === reel2 || reel2 === reel3 || reel1 === reel3) {
    multiplier = 2; // Any 2 match
  } else if (reel1 === 'CHERRY' || reel2 === 'CHERRY' || reel3 === 'CHERRY') {
    multiplier = 1; // Any single cherry returns bet
  }

  const winAmount = betAmount * multiplier;
  user.balance += winAmount;

  if (winAmount > user.biggestWin) {
    user.biggestWin = winAmount;
  }

  if (isJackpot) {
    io.emit('jackpot_announcement', {
      userName: user.name,
      symbol: reel1,
      winAmount
    });
  }

  res.json({
    reels: [reel1, reel2, reel3],
    winAmount,
    multiplier,
    isJackpot,
    newBalance: user.balance
  });
});

// ==========================================
// 5. SOCKET.IO REAL-TIME CONNECTIONS
// ==========================================

io.on('connection', (socket) => {
  console.log(`[Socket] Client connected: ${socket.id}`);

  // Join Room
  socket.on('join_room', ({ roomId, userId, userName }) => {
    socket.join(roomId);
    socket.userId = userId;
    socket.userName = userName;

    console.log(`[Socket] User ${userName || userId} joined ${roomId}`);

    if (roomId === 'roulette-main') {
      socket.emit('roulette_room_state', {
        phase: rouletteRoom.phase,
        timeLeft: rouletteRoom.timeLeft,
        bets: rouletteRoom.bets,
        history: rouletteRoom.history,
        activeBots: BOTS
      });
    } else if (roomId === 'coinflip-main') {
      socket.emit('coinflip_room_state', {
        phase: coinflipRoom.phase,
        timeLeft: coinflipRoom.timeLeft,
        bets: coinflipRoom.bets,
        history: coinflipRoom.history
      });
    }
  });

  // Roulette Bet Placement
  socket.on('place_roulette_bet', ({ userId, userName, type, value, amount }) => {
    if (rouletteRoom.phase !== 'BETTING') {
      return socket.emit('bet_rejected', { reason: 'Betting phase is closed' });
    }

    const user = getOrCreateUser(userId, userName);
    if (user.balance < amount) {
      return socket.emit('bet_rejected', { reason: 'Insufficient balance' });
    }

    user.balance -= amount;

    const bet = {
      id: `${Date.now()}_${Math.random().toString(36).substr(2, 5)}`,
      userId,
      userName: userName || user.name,
      type,
      value,
      amount,
      isBot: false
    };

    rouletteRoom.bets.push(bet);

    io.to('roulette-main').emit('roulette_bets_update', rouletteRoom.bets);
    socket.emit('bet_accepted', { bet, newBalance: user.balance });
  });

  // Coinflip Bet Placement
  socket.on('place_coinflip_bet', ({ userId, userName, choice, amount }) => {
    if (coinflipRoom.phase !== 'BETTING') {
      return socket.emit('bet_rejected', { reason: 'Betting window closed' });
    }

    const user = getOrCreateUser(userId, userName);
    if (user.balance < amount) {
      return socket.emit('bet_rejected', { reason: 'Insufficient balance' });
    }

    user.balance -= amount;

    const bet = {
      id: `${Date.now()}_${Math.random().toString(36).substr(2, 5)}`,
      userId,
      userName: userName || user.name,
      choice,
      amount,
      isBot: false
    };

    coinflipRoom.bets.push(bet);
    io.to('coinflip-main').emit('coinflip_bets_update', coinflipRoom.bets);
    socket.emit('bet_accepted', { bet, newBalance: user.balance });
  });

  // ==========================================
  // 1v1 PRIVATE DUEL ROOM SYSTEM (BY ROOM CODE)
  // ==========================================
  socket.on('create_duel_room', ({ userId, userName, userAvatar, stake }) => {
    const code = Math.floor(100000 + Math.random() * 900000).toString();
    const room = {
      code,
      stake: stake || 1000,
      pot: (stake || 1000) * 2,
      phase: 'WAITING_FOR_PLAYER',
      roundNumber: 1,
      host: { id: userId, name: userName || 'Host', avatar: userAvatar || '👑', choice: 'HEADS', socketId: socket.id },
      guest: null,
      result: null,
      winnerId: null,
      winnerName: null
    };
    duelRooms.set(code, room);
    socket.join(`duel_${code}`);
    socket.emit('duel_room_created', room);
    console.log(`[Duel] Room #${code} created by ${userName} with stake ${stake}`);
  });

  socket.on('join_duel_room', ({ code, userId, userName, userAvatar }) => {
    const cleanCode = (code || '').trim();
    const room = duelRooms.get(cleanCode);
    if (!room) {
      return socket.emit('duel_error', { message: `Room #${cleanCode} not found` });
    }
    if (room.guest && room.guest.id !== userId) {
      return socket.emit('duel_error', { message: `Room #${cleanCode} is already full` });
    }

    if (!room.guest) {
      room.guest = {
        id: userId,
        name: userName || 'Guest',
        avatar: userAvatar || '🦁',
        choice: room.host.choice === 'HEADS' ? 'TAILS' : 'HEADS',
        socketId: socket.id
      };
      room.phase = 'SELECTING';
    }

    socket.join(`duel_${cleanCode}`);
    io.to(`duel_${cleanCode}`).emit('duel_room_updated', room);
    console.log(`[Duel] User ${userName} joined room #${cleanCode}`);
  });

  socket.on('duel_select_choice', ({ code, userId, choice }) => {
    const room = duelRooms.get(code);
    if (!room) return;
    const isHost = room.host.id === userId;
    const otherChoice = choice === 'HEADS' ? 'TAILS' : 'HEADS';

    if (isHost) {
      room.host.choice = choice;
      if (room.guest) room.guest.choice = otherChoice;
    } else if (room.guest) {
      room.guest.choice = choice;
      room.host.choice = otherChoice;
    }

    io.to(`duel_${code}`).emit('duel_room_updated', room);
  });

  socket.on('duel_start_flip', ({ code }) => {
    const room = duelRooms.get(code);
    if (!room || room.phase === 'FLIPPING') return;

    room.phase = 'FLIPPING';
    io.to(`duel_${code}`).emit('duel_room_updated', room);

    setTimeout(() => {
      const outcome = Math.random() < 0.5 ? 'HEADS' : 'TAILS';
      const hostWon = room.host.choice === outcome;
      const winner = hostWon ? room.host : room.guest;

      room.phase = 'PAYOUT';
      room.result = outcome;
      room.winnerId = winner.id;
      room.winnerName = winner.name;

      const winnerUser = getOrCreateUser(winner.id, winner.name);
      winnerUser.balance += room.pot;
      winnerUser.duelWins = (winnerUser.duelWins || 0) + 1;
      winnerUser.duelsPlayed = (winnerUser.duelsPlayed || 0) + 1;

      const loser = hostWon ? room.guest : room.host;
      if (loser) {
        const loserUser = getOrCreateUser(loser.id, loser.name);
        loserUser.duelsPlayed = (loserUser.duelsPlayed || 0) + 1;
      }

      broadcastLeaderboard();
      io.to(`duel_${code}`).emit('duel_room_updated', room);
    }, 2500);
  });

  socket.on('duel_rematch', ({ code }) => {
    const room = duelRooms.get(code);
    if (!room) return;
    room.phase = 'SELECTING';
    room.result = null;
    room.winnerId = null;
    room.winnerName = null;
    room.roundNumber += 1;
    io.to(`duel_${code}`).emit('duel_room_updated', room);
  });

  socket.on('duel_leave', ({ code, userId }) => {
    const room = duelRooms.get(code);
    if (!room) return;
    socket.leave(`duel_${code}`);
    if (room.host.id === userId) {
      io.to(`duel_${code}`).emit('duel_error', { message: 'Host has closed the room' });
      duelRooms.delete(code);
    } else if (room.guest && room.guest.id === userId) {
      room.guest = null;
      room.phase = 'WAITING_FOR_PLAYER';
      io.to(`duel_${code}`).emit('duel_room_updated', room);
    }
  });

  socket.on('disconnect', () => {
    console.log(`[Socket] Client disconnected: ${socket.id}`);
  });
});

server.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(` VegasPulse Casino Game Server listening on port ${PORT}`);
  console.log(` - Roulette Room Active (10s Bet / 5s Spin / 3s Payout)`);
  console.log(` - Coinflip Room Active (8s Bet / 3s Flip / 3s Payout)`);
  console.log(` - Simulated AI Bots: ${BOTS.length} connected`);
  console.log(`====================================================`);
});
