import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import socketService from '../services/socketService';

const STORAGE_KEY_USER = '@vegaspulse_user_session';

export const useCasinoStore = create((set, get) => ({
  // User Profile & Virtual Wallet (100% Free Virtual Currency)
  userId: 'user_' + Math.random().toString(36).substr(2, 6),
  userName: 'Guest VIP',
  balance: 5000,
  streakDay: 1,
  lastClaimTime: 0,
  biggestWin: 0,
  winStreak: 0,
  
  // Real-time Roulette Room State
  roulettePhase: 'BETTING', // 'BETTING' | 'SPINNING' | 'PAYOUT'
  rouletteTimeLeft: 10,
  rouletteBets: [],
  rouletteWinningNumber: null,
  rouletteWinningColor: null,
  rouletteHistory: [17, 32, 0, 29, 7],
  
  // Coinflip State
  coinflipPhase: 'BETTING',
  coinflipTimeLeft: 8,
  coinflipBets: [],
  coinflipResult: null,
  
  // Global Leaderboards
  leaderboards: {
    byBalance: [],
    byBiggestWin: [],
    byWinStreak: []
  },
  
  // Notification / Toast
  toastMessage: null,

  // Load Saved Guest Session
  initSession: async () => {
    try {
      const saved = await AsyncStorage.getItem(STORAGE_KEY_USER);
      if (saved) {
        const parsed = JSON.parse(saved);
        set({
          userId: parsed.userId,
          userName: parsed.userName,
          balance: parsed.balance,
          streakDay: parsed.streakDay || 1,
          lastClaimTime: parsed.lastClaimTime || 0,
          biggestWin: parsed.biggestWin || 0,
          winStreak: parsed.winStreak || 0
        });
      }
    } catch (e) {
      console.warn('Failed to load session', e);
    }

    const state = get();
    socketService.connect(state.userId, state.userName);

    // Bind Socket Listeners
    socketService.on('roulette_room_state', (data) => {
      set({
        roulettePhase: data.phase,
        rouletteTimeLeft: data.timeLeft,
        rouletteBets: data.bets,
        rouletteHistory: data.history.map(h => h.num)
      });
    });

    socketService.on('roulette_tick', (tick) => {
      set({
        roulettePhase: tick.phase,
        rouletteTimeLeft: tick.timeLeft
      });
    });

    socketService.on('roulette_bets_update', (bets) => {
      set({ rouletteBets: bets });
    });

    socketService.on('roulette_spin_start', (data) => {
      set({
        roulettePhase: 'SPINNING',
        rouletteTimeLeft: data.durationSeconds,
        rouletteWinningNumber: data.winningNumber,
        rouletteWinningColor: data.winningColor
      });
    });

    socketService.on('roulette_payout', (data) => {
      set((prev) => {
        const myPayout = data.payouts.find(p => p.userId === prev.userId);
        let newBal = prev.balance;
        let toast = null;
        if (myPayout && myPayout.won) {
          newBal += myPayout.payout;
          toast = `🎉 Won ${myPayout.payout.toLocaleString()} coins on Roulette #${data.winningNumber}!`;
        }
        return {
          roulettePhase: 'PAYOUT',
          rouletteWinningNumber: data.winningNumber,
          rouletteWinningColor: data.winningColor,
          rouletteHistory: data.history.map(h => h.num),
          balance: newBal,
          toastMessage: toast
        };
      });
      get().saveSession();
    });

    socketService.on('bet_accepted', ({ newBalance }) => {
      set({ balance: newBalance });
      get().saveSession();
    });

    socketService.on('leaderboard_update', (boards) => {
      set({ leaderboards: boards });
    });
  },

  saveSession: async () => {
    const s = get();
    try {
      await AsyncStorage.setItem(STORAGE_KEY_USER, JSON.stringify({
        userId: s.userId,
        userName: s.userName,
        balance: s.balance,
        streakDay: s.streakDay,
        lastClaimTime: s.lastClaimTime,
        biggestWin: s.biggestWin,
        winStreak: s.winStreak
      }));
    } catch (err) {
      console.warn('Failed to save session', err);
    }
  },

  // Daily Claim Logic
  claimDailyReward: () => {
    const now = Date.now();
    const s = get();
    const ONE_DAY = 24 * 60 * 60 * 1000;
    const GRACE = 48 * 60 * 60 * 1000;

    if (s.lastClaimTime > 0 && now - s.lastClaimTime < ONE_DAY) {
      return { success: false, remainingMs: ONE_DAY - (now - s.lastClaimTime) };
    }

    let nextDay = 1;
    if (s.lastClaimTime > 0 && now - s.lastClaimTime <= GRACE) {
      nextDay = (s.streakDay % 7) + 1;
    }

    const rewards = [500, 1000, 2000, 3500, 5000, 7500, 15000];
    const reward = rewards[nextDay - 1];

    set((prev) => ({
      balance: prev.balance + reward,
      streakDay: nextDay,
      lastClaimTime: now,
      toastMessage: `🎁 Claimed Day ${nextDay} Bonus: +${reward.toLocaleString()} Coins!`
    }));

    get().saveSession();
    return { success: true, reward, streakDay: nextDay };
  },

  placeRouletteBet: (type, value, amount) => {
    const s = get();
    if (s.roulettePhase !== 'BETTING') return false;
    if (s.balance < amount) return false;

    socketService.placeRouletteBet({
      userId: s.userId,
      userName: s.userName,
      type,
      value,
      amount
    });
    return true;
  },

  updateSlotWin: (winAmount, multiplier, isJackpot) => {
    set((prev) => {
      const newBal = prev.balance + winAmount;
      const biggest = Math.max(prev.biggestWin, winAmount);
      return {
        balance: newBal,
        biggestWin: biggest,
        toastMessage: isJackpot 
          ? `🌟 MEGA JACKPOT! +${winAmount.toLocaleString()} COINS!` 
          : winAmount > 0 ? `Won +${winAmount.toLocaleString()} coins!` : null
      };
    });
    get().saveSession();
  },

  clearToast: () => set({ toastMessage: null })
}));
