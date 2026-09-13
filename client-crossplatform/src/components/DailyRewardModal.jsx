import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity } from 'react-native';
import { useCasinoStore } from '../store/casinoStore';

export default function DailyRewardModal({ visible, onClose }) {
  const { streakDay, lastClaimTime, claimDailyReward } = useCasinoStore();
  const [timeLeft, setTimeLeft] = useState('');
  const [canClaim, setCanClaim] = useState(false);

  const REWARDS = [
    { day: 1, amount: 500 },
    { day: 2, amount: 1000 },
    { day: 3, amount: 2000 },
    { day: 4, amount: 3500 },
    { day: 5, amount: 5000 },
    { day: 6, amount: 7500 },
    { day: 7, amount: 15000, special: '👑 JACKPOT CHEST' }
  ];

  useEffect(() => {
    const updateCountdown = () => {
      const now = Date.now();
      const ONE_DAY = 24 * 60 * 60 * 1000;
      const nextClaim = lastClaimTime + ONE_DAY;

      if (now >= nextClaim || lastClaimTime === 0) {
        setCanClaim(true);
        setTimeLeft('READY TO CLAIM!');
      } else {
        setCanClaim(false);
        const diff = nextClaim - now;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        setTimeLeft(`${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`);
      }
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 1000);
    return () => clearInterval(interval);
  }, [lastClaimTime]);

  const handleClaim = () => {
    const res = claimDailyReward();
    if (res.success) {
      setTimeout(() => onClose(), 800);
    }
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.modalContent}>
          <Text style={styles.headerTitle}>🎁 7-Day Streak Multiplier</Text>
          <Text style={styles.subtitle}>Claim 100% free virtual coins every 24 hours!</Text>

          {/* 7-Day Roadmap Grid */}
          <View style={styles.roadmapGrid}>
            {REWARDS.map((r) => {
              const isCurrent = r.day === streakDay;
              const isCompleted = r.day < streakDay;

              return (
                <View
                  key={r.day}
                  style={[
                    styles.dayBox,
                    isCurrent && styles.activeDayBox,
                    isCompleted && styles.completedDayBox
                  ]}
                >
                  <Text style={styles.dayLabel}>Day {r.day}</Text>
                  <Text style={styles.coinAmount}>+{r.amount.toLocaleString()}</Text>
                  {r.special && <Text style={styles.specialTag}>{r.special}</Text>}
                  {isCompleted && <Text style={styles.checkMark}>✓</Text>}
                </View>
              );
            })}
          </View>

          {/* Countdown & Action */}
          <View style={styles.timerContainer}>
            <Text style={styles.timerLabel}>Next Claim Available In:</Text>
            <Text style={[styles.timerValue, canClaim && styles.readyTimer]}>{timeLeft}</Text>
          </View>

          <TouchableOpacity
            style={[styles.claimButton, !canClaim && styles.disabledButton]}
            disabled={!canClaim}
            onPress={handleClaim}
          >
            <Text style={styles.claimButtonText}>
              {canClaim ? `CLAIM DAY ${streakDay} REWARD` : 'COME BACK LATER'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.closeButton} onPress={onClose}>
            <Text style={styles.closeText}>Close</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.85)', justifyContent: 'center', alignItems: 'center', padding: 20 },
  modalContent: { width: '100%', maxWidth: 420, backgroundColor: '#130C29', borderRadius: 24, padding: 22, alignItems: 'center', borderWidth: 1, borderColor: '#3B2968' },
  headerTitle: { fontSize: 22, fontWeight: '900', color: '#FBBF24', marginBottom: 4 },
  subtitle: { fontSize: 12, color: '#A5B4FC', textAlign: 'center', marginBottom: 18 },
  roadmapGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'center', gap: 8, marginBottom: 18 },
  dayBox: { width: '30%', backgroundColor: '#1E153D', borderRadius: 12, padding: 10, alignItems: 'center', borderWidth: 1, borderColor: '#31235F' },
  activeDayBox: { borderColor: '#F59E0B', backgroundColor: '#382269', transform: [{ scale: 1.05 }] },
  completedDayBox: { opacity: 0.6, borderColor: '#10B981' },
  dayLabel: { color: '#9CA3AF', fontSize: 11, fontWeight: '700' },
  coinAmount: { color: '#FCD34D', fontSize: 13, fontWeight: '800', marginTop: 2 },
  specialTag: { color: '#F43F5E', fontSize: 9, fontWeight: '800', textAlign: 'center', marginTop: 2 },
  checkMark: { color: '#10B981', fontSize: 14, fontWeight: '900', marginTop: 2 },
  timerContainer: { alignItems: 'center', marginBottom: 16 },
  timerLabel: { color: '#9CA3AF', fontSize: 12 },
  timerValue: { color: '#EF4444', fontSize: 18, fontWeight: '800', marginTop: 2 },
  readyTimer: { color: '#10B981' },
  claimButton: { width: '100%', backgroundColor: '#10B981', paddingVertical: 14, borderRadius: 14, alignItems: 'center', shadowColor: '#10B981', shadowOpacity: 0.4, shadowRadius: 10 },
  disabledButton: { backgroundColor: '#374151', opacity: 0.7 },
  claimButtonText: { color: '#FFF', fontWeight: '900', fontSize: 15 },
  closeButton: { marginTop: 14, padding: 6 },
  closeText: { color: '#9CA3AF', fontSize: 13 }
});
