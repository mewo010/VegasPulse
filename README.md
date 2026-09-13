# 🎰 VegasPulse - Cross-Platform Multiplayer Casino & Systems Architecture

A production-ready, cross-platform real-time multiplayer casino application designed for **Android (`.apk` / `.aab`)**, **iOS (`.ipa`)**, and **Windows Desktop (`.exe`)**, supported by a high-performance **Node.js + Express + Socket.io** authoritative game server and unified **GitHub Actions CI/CD pipeline**.

---

## 🏗️ 1. Complete Project Structure

```text
├── .github/
│   └── workflows/
│       └── build-and-release.yml    # Unified multi-platform CI/CD release workflow
├── server/
│   ├── package.json                 # Node.js + Socket.io server dependencies
│   └── server.js                    # Server-authoritative game loops, AI bots & APIs
├── client-crossplatform/
│   ├── app.json                     # Expo & React Native configuration
│   ├── package.json                 # React Native / Desktop dependencies
│   ├── App.js                       # Main application shell with navigation
│   └── src/
│       ├── services/
│       │   └── socketService.js     # Real-time WebSocket connection manager
│       ├── store/
│       │   └── casinoStore.js       # Zustand synchronized state & local storage
│       └── components/
│           ├── RouletteTable.jsx    # Real-time Roulette betting board & sync
│           ├── SlotMachine.jsx      # 3-Reel animated slots & jackpot engine
│           ├── Leaderboard.jsx      # Global rankings (Balance, Win, Streaks)
│           └── DailyRewardModal.jsx # 24h countdown & 7-day streak claim modal
├── app/                             # Native Android Jetpack Compose client
│   ├── build.gradle.kts             # Gradle Android app configuration
│   └── src/main/java/com/example/   # Native Android Kotlin UI & multiplayer engine
└── README.md
```

---

## ⚡ 2. Game Architecture & Core Features

### Server-Authoritative Timing Loop
- **Phase 1: Betting (10 seconds)**: Clients place chip bets on board numbers (0-36), Red/Black, Even/Odd, 1-18/19-36.
- **Phase 2: Spinning (5 seconds)**: The server randomly determines the outcome and broadcasts rotation commands to all connected devices simultaneously.
- **Phase 3: Payout (3 seconds)**: Server computes mathematical payouts (up to 35:1 straight-up), updates global player balances, and broadcasts leaderboard adjustments.

### Intelligent Automated Server Bots
- Pre-seeded with 6 unique bots (`VIP_Viper`, `LuckyLucy`, `HighRoller_Max`, `AceQueen`, `GoldenSam`, `CyberWhale`).
- Bots calculate and place variable randomized chip amounts on active tables during the betting window, guaranteeing active room density.

### Virtual Currency & 24-Hour Streak System
- **100% Free Virtual Currency**: Strictly entertainment gaming with zero real money.
- **7-Day Streak Rewards**:
  - Day 1: 500 coins
  - Day 2: 1,000 coins
  - Day 3: 2,000 coins
  - Day 4: 3,500 coins
  - Day 5: 5,000 coins
  - Day 6: 7,500 coins
  - Day 7: 15,000 coins + 👑 Jackpot Bonus

---

## 🚀 3. Quick Start & Setup Guide

### Running the Node.js / Socket.io Backend Server
```bash
cd server
npm install
npm start
# Server starts on http://localhost:3001 with active WebSocket rooms
```

### Running the Cross-Platform Client (Expo / Desktop)
```bash
cd client-crossplatform
npm install

# Run on Android emulator / device:
npm run android

# Run on iOS simulator (macOS required):
npm run ios

# Run on Windows Desktop:
npm run windows
```

### GitHub Actions CI/CD Build
Pushes to `main` or tags matching `v*` will trigger `.github/workflows/build-and-release.yml` to:
1. Build release Android APK & AAB (`ubuntu-latest`).
2. Build unsigned iOS `.ipa` (`macos-latest`).
3. Build standalone Windows `.exe` (`windows-latest`).
4. Publish all compiled binaries directly to GitHub Releases.
