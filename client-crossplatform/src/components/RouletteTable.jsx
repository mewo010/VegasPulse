import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { useCasinoStore } from '../store/casinoStore';

export default function RouletteTable() {
  const {
    roulettePhase,
    rouletteTimeLeft,
    rouletteBets,
    rouletteWinningNumber,
    rouletteWinningColor,
    rouletteHistory,
    placeRouletteBet,
    balance
  } = useCasinoStore();

  const [selectedChip, setSelectedChip] = useState(100);
  const CHIP_VALUES = [25, 50, 100, 500, 1000];

  const handleBet = (type, value) => {
    if (balance < selectedChip) {
      alert('Insufficient virtual coins!');
      return;
    }
    placeRouletteBet(type, value, selectedChip);
  };

  const getPhaseColor = () => {
    if (roulettePhase === 'BETTING') return '#00E676';
    if (roulettePhase === 'SPINNING') return '#FFB300';
    return '#E53935';
  };

  return (
    <View style={styles.container}>
      {/* Timer & Status Bar */}
      <View style={styles.statusHeader}>
        <View style={styles.phaseBadge}>
          <Text style={[styles.phaseText, { color: getPhaseColor() }]}>
            {roulettePhase === 'BETTING' && `⏱️ BETTING CLOSES IN ${rouletteTimeLeft}s`}
            {roulettePhase === 'SPINNING' && `🎡 WHEEL SPINNING... (${rouletteTimeLeft}s)`}
            {roulettePhase === 'PAYOUT' && `🎉 PAYOUT! WINNER: #${rouletteWinningNumber} (${rouletteWinningColor?.toUpperCase()})`}
          </Text>
        </View>
        <Text style={styles.betCountText}>{rouletteBets.length} Bets Placed</Text>
      </View>

      {/* History Ribbon */}
      <View style={styles.historyRow}>
        <Text style={styles.historyLabel}>Recent:</Text>
        {rouletteHistory.map((num, idx) => (
          <View
            key={idx}
            style={[
              styles.historyBadge,
              { backgroundColor: num === 0 ? '#10B981' : [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36].includes(num) ? '#EF4444' : '#1F2937' }
            ]}
          >
            <Text style={styles.historyText}>{num}</Text>
          </View>
        ))}
      </View>

      {/* Chip Selector */}
      <View style={styles.chipRow}>
        {CHIP_VALUES.map((chip) => (
          <TouchableOpacity
            key={chip}
            style={[styles.chipButton, selectedChip === chip && styles.selectedChip]}
            onPress={() => setSelectedChip(chip)}
          >
            <Text style={styles.chipText}>${chip}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Roulette Betting Grid */}
      <ScrollView horizontal contentContainerStyle={styles.boardContainer}>
        <View>
          {/* Zero Column */}
          <TouchableOpacity
            style={[styles.gridCell, styles.zeroCell]}
            onPress={() => handleBet('NUMBER', 0)}
            disabled={roulettePhase !== 'BETTING'}
          >
            <Text style={styles.cellText}>0</Text>
          </TouchableOpacity>

          {/* Outside Bets */}
          <View style={styles.outsideRow}>
            <TouchableOpacity
              style={[styles.outsideButton, { backgroundColor: '#DC2626' }]}
              onPress={() => handleBet('RED', 'RED')}
              disabled={roulettePhase !== 'BETTING'}
            >
              <Text style={styles.outsideText}>RED</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.outsideButton, { backgroundColor: '#18181B' }]}
              onPress={() => handleBet('BLACK', 'BLACK')}
              disabled={roulettePhase !== 'BETTING'}
            >
              <Text style={styles.outsideText}>BLACK</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.outsideButton}
              onPress={() => handleBet('EVEN', 'EVEN')}
              disabled={roulettePhase !== 'BETTING'}
            >
              <Text style={styles.outsideText}>EVEN</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.outsideButton}
              onPress={() => handleBet('ODD', 'ODD')}
              disabled={roulettePhase !== 'BETTING'}
            >
              <Text style={styles.outsideText}>ODD</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>

      {/* Live Bots & Players Bets Feed */}
      <View style={styles.liveBetsFeed}>
        <Text style={styles.feedTitle}>⚡ Real-Time Table Activity</Text>
        <ScrollView style={styles.feedScroll}>
          {rouletteBets.slice(-4).map((bet, idx) => (
            <View key={idx} style={styles.betItem}>
              <Text style={styles.betUser}>
                {bet.isBot ? '🤖 ' : '👤 '}{bet.userName}:
              </Text>
              <Text style={styles.betDetails}>
                ${bet.amount} on {bet.type} {bet.value !== bet.type ? `(${bet.value})` : ''}
              </Text>
            </View>
          ))}
        </ScrollView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { backgroundColor: '#130E26', borderRadius: 16, padding: 14, marginVertical: 10 },
  statusHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  phaseBadge: { backgroundColor: '#1F173D', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 20 },
  phaseText: { fontWeight: '700', fontSize: 12 },
  betCountText: { color: '#9CA3AF', fontSize: 12 },
  historyRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  historyLabel: { color: '#D1D5DB', fontSize: 12, marginRight: 8 },
  historyBadge: { width: 26, height: 26, borderRadius: 13, justifyContent: 'center', alignItems: 'center', marginRight: 6 },
  historyText: { color: '#FFF', fontSize: 11, fontWeight: '700' },
  chipRow: { flexDirection: 'row', justifyContent: 'space-around', marginVertical: 10 },
  chipButton: { width: 48, height: 48, borderRadius: 24, backgroundColor: '#2C1F54', justifyContent: 'center', alignItems: 'center', borderWidth: 2, borderColor: '#7C3AED' },
  selectedChip: { borderColor: '#F59E0B', backgroundColor: '#4C1D95', transform: [{ scale: 1.1 }] },
  chipText: { color: '#F3F4F6', fontWeight: '800', fontSize: 13 },
  boardContainer: { paddingVertical: 10 },
  gridCell: { width: 60, height: 40, justifyContent: 'center', alignItems: 'center', margin: 2, borderRadius: 6 },
  zeroCell: { backgroundColor: '#10B981', width: 250 },
  cellText: { color: '#FFF', fontWeight: '700' },
  outsideRow: { flexDirection: 'row', marginTop: 8 },
  outsideButton: { flex: 1, paddingVertical: 10, paddingHorizontal: 14, backgroundColor: '#2A1F4D', marginHorizontal: 3, borderRadius: 8, alignItems: 'center' },
  outsideText: { color: '#F3F4F6', fontWeight: '700', fontSize: 12 },
  liveBetsFeed: { marginTop: 14, backgroundColor: '#0B0817', padding: 10, borderRadius: 10 },
  feedTitle: { color: '#FBBF24', fontSize: 12, fontWeight: '700', marginBottom: 6 },
  feedScroll: { maxHeight: 80 },
  betItem: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 2 },
  betUser: { color: '#E5E7EB', fontSize: 11, fontWeight: '600' },
  betDetails: { color: '#34D399', fontSize: 11, fontWeight: '700' }
});
