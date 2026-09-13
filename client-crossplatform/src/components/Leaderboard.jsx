import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList } from 'react-native';
import { useCasinoStore } from '../store/casinoStore';

export default function Leaderboard() {
  const { leaderboards } = useCasinoStore();
  const [activeTab, setActiveTab] = useState('balance'); // 'balance' | 'biggestWin' | 'winStreak'

  const getList = () => {
    if (activeTab === 'balance') return leaderboards.byBalance || [];
    if (activeTab === 'biggestWin') return leaderboards.byBiggestWin || [];
    return leaderboards.byWinStreak || [];
  };

  const getMetric = (item) => {
    if (activeTab === 'balance') return `${item.balance.toLocaleString()} Coins`;
    if (activeTab === 'biggestWin') return `+${item.biggestWin.toLocaleString()}`;
    return `${item.winStreak} Wins`;
  };

  const renderItem = ({ item, index }) => {
    const isPodium = index < 3;
    const podiumColors = ['#F59E0B', '#94A3B8', '#B45309'];

    return (
      <View style={[styles.card, index === 0 && styles.firstCard]}>
        <View style={styles.rankBadge}>
          <Text style={[styles.rankText, isPodium && { color: podiumColors[index], fontWeight: '900' }]}>
            {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `#${index + 1}`}
          </Text>
        </View>
        <Text style={styles.avatar}>{item.avatar || (item.isBot ? '🤖' : '🎰')}</Text>
        <View style={styles.userInfo}>
          <Text style={styles.userName}>
            {item.name} {item.isBot ? <Text style={styles.botTag}>BOT</Text> : null}
          </Text>
        </View>
        <Text style={styles.metricText}>{getMetric(item)}</Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🏆 Global High Rollers</Text>
      
      {/* Category Tabs */}
      <View style={styles.tabBar}>
        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'balance' && styles.activeTab]}
          onPress={() => setActiveTab('balance')}
        >
          <Text style={[styles.tabText, activeTab === 'balance' && styles.activeTabText]}>💰 Balance</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'biggestWin' && styles.activeTab]}
          onPress={() => setActiveTab('biggestWin')}
        >
          <Text style={[styles.tabText, activeTab === 'biggestWin' && styles.activeTabText]}>⚡ Best Win</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'winStreak' && styles.activeTab]}
          onPress={() => setActiveTab('winStreak')}
        >
          <Text style={[styles.tabText, activeTab === 'winStreak' && styles.activeTabText]}>🔥 Streaks</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={getList()}
        keyExtractor={(item, index) => item.id || index.toString()}
        renderItem={renderItem}
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D091A', padding: 16, borderRadius: 16 },
  title: { fontSize: 20, fontWeight: '800', color: '#FBBF24', textAlign: 'center', marginBottom: 16 },
  tabBar: { flexDirection: 'row', backgroundColor: '#1A1233', borderRadius: 12, padding: 4, marginBottom: 16 },
  tabButton: { flex: 1, paddingVertical: 10, alignItems: 'center', borderRadius: 8 },
  activeTab: { backgroundColor: '#7C3AED' },
  tabText: { color: '#9CA3AF', fontWeight: '700', fontSize: 12 },
  activeTabText: { color: '#FFF' },
  listContainer: { paddingBottom: 20 },
  card: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#181130', borderRadius: 12, padding: 12, marginBottom: 8, borderWidth: 1, borderColor: '#2E2254' },
  firstCard: { borderColor: '#F59E0B', backgroundColor: '#231647' },
  rankBadge: { width: 32, alignItems: 'center' },
  rankText: { color: '#E5E7EB', fontSize: 14, fontWeight: '700' },
  avatar: { fontSize: 22, marginHorizontal: 8 },
  userInfo: { flex: 1 },
  userName: { color: '#F3F4F6', fontSize: 14, fontWeight: '700' },
  botTag: { fontSize: 9, color: '#A78BFA', fontWeight: '800' },
  metricText: { color: '#34D399', fontSize: 14, fontWeight: '800' }
});
