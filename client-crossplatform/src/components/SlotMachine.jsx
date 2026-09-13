import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Animated } from 'react-native';
import { useCasinoStore } from '../store/casinoStore';

const SYMBOLS = ['🍒', '🍋', '🔔', '💎', '7️⃣', '⭐'];

export default function SlotMachine() {
  const { balance, updateSlotWin } = useCasinoStore();
  const [reels, setReels] = useState(['7️⃣', '💎', '7️⃣']);
  const [isSpinning, setIsSpinning] = useState(false);
  const [bet, setBet] = useState(100);
  const [lastWin, setLastWin] = useState(0);

  const handleSpin = () => {
    if (balance < bet || isSpinning) return;

    setIsSpinning(true);
    setLastWin(0);

    // Simulated quick animated ticker
    let spins = 0;
    const interval = setInterval(() => {
      setReels([
        SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)],
        SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)],
        SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)]
      ]);
      spins++;
      if (spins > 15) {
        clearInterval(interval);
        finalizeSpin();
      }
    }, 80);
  };

  const finalizeSpin = () => {
    // Generate server-authoritative style outcome
    const r1 = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
    const r2 = Math.random() > 0.4 ? r1 : SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
    const r3 = Math.random() > 0.6 ? r1 : SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];

    setReels([r1, r2, r3]);
    setIsSpinning(false);

    let win = 0;
    let isJackpot = false;
    if (r1 === r2 && r2 === r3) {
      if (r1 === '7️⃣') { win = bet * 100; isJackpot = true; }
      else if (r1 === '💎') { win = bet * 50; isJackpot = true; }
      else { win = bet * 20; }
    } else if (r1 === r2 || r2 === r3 || r1 === r3) {
      win = bet * 2;
    }

    setLastWin(win);
    updateSlotWin(win - bet, win > 0 ? win / bet : 0, isJackpot);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🎰 Cyber Jackpot Slots</Text>

      {/* Reel Box */}
      <View style={styles.reelFrame}>
        {reels.map((s, i) => (
          <View key={i} style={styles.reelSlot}>
            <Text style={styles.reelSymbol}>{s}</Text>
          </View>
        ))}
      </View>

      {/* Payout Display */}
      {lastWin > 0 && (
        <View style={styles.winBanner}>
          <Text style={styles.winText}>🎉 WINNER! +{lastWin.toLocaleString()} COINS!</Text>
        </View>
      )}

      {/* Bet Controls */}
      <View style={styles.betRow}>
        {[50, 100, 250, 500].map(b => (
          <TouchableOpacity
            key={b}
            style={[styles.betBtn, bet === b && styles.activeBetBtn]}
            onPress={() => setBet(b)}
            disabled={isSpinning}
          >
            <Text style={[styles.betBtnText, bet === b && styles.activeBetBtnText]}>${b}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Spin Button */}
      <TouchableOpacity
        style={[styles.spinButton, (isSpinning || balance < bet) && styles.disabledButton]}
        onPress={handleSpin}
        disabled={isSpinning || balance < bet}
      >
        <Text style={styles.spinButtonText}>{isSpinning ? 'SPINNING...' : 'SPIN REELS'}</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { backgroundColor: '#130C29', borderRadius: 20, padding: 18, alignItems: 'center', marginVertical: 12 },
  title: { fontSize: 18, fontWeight: '800', color: '#FBBF24', marginBottom: 16 },
  reelFrame: { flexDirection: 'row', backgroundColor: '#07040F', padding: 14, borderRadius: 18, borderWidth: 2, borderColor: '#7C3AED', gap: 12 },
  reelSlot: { width: 72, height: 90, backgroundColor: '#1F143D', borderRadius: 12, justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: '#4C1D95' },
  reelSymbol: { fontSize: 38 },
  winBanner: { backgroundColor: '#10B981', paddingVertical: 8, paddingHorizontal: 16, borderRadius: 12, marginTop: 12 },
  winText: { color: '#FFF', fontWeight: '900', fontSize: 14 },
  betRow: { flexDirection: 'row', gap: 8, marginVertical: 14 },
  betBtn: { paddingVertical: 8, paddingHorizontal: 14, backgroundColor: '#1E153E', borderRadius: 8, borderWidth: 1, borderColor: '#3B2968' },
  activeBetBtn: { borderColor: '#F59E0B', backgroundColor: '#382269' },
  betBtnText: { color: '#9CA3AF', fontWeight: '700', fontSize: 12 },
  activeBetBtnText: { color: '#FBBF24' },
  spinButton: { width: '100%', backgroundColor: '#F59E0B', paddingVertical: 14, borderRadius: 14, alignItems: 'center' },
  disabledButton: { opacity: 0.5 },
  spinButtonText: { color: '#111827', fontWeight: '900', fontSize: 16 }
});
