import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, SafeAreaView, TouchableOpacity, StatusBar } from 'react-native';
import { useCasinoStore } from './src/store/casinoStore';
import RouletteTable from './src/components/RouletteTable';
import Leaderboard from './src/components/Leaderboard';
import SlotMachine from './src/components/SlotMachine';
import DailyRewardModal from './src/components/DailyRewardModal';

export default function App() {
  const { balance, streakDay, initSession, toastMessage, clearToast } = useCasinoStore();
  const [currentTab, setCurrentTab] = useState('roulette'); // 'roulette' | 'slots' | 'leaderboard'
  const [showDailyModal, setShowDailyModal] = useState(false);

  useEffect(() => {
    initSession();
  }, []);

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor="#0B0817" />

      {/* Header Bar */}
      <View style={styles.header}>
        <View>
          <Text style={styles.brandTitle}>VEGAS<Text style={styles.brandAccent}>PULSE</Text></Text>
          <Text style={styles.subBrand}>Multiplayer Live Casino</Text>
        </View>

        <View style={styles.headerActions}>
          {/* Daily Streak Trigger */}
          <TouchableOpacity
            style={styles.streakBadge}
            onPress={() => setShowDailyModal(true)}
          >
            <Text style={styles.streakIcon}>🎁</Text>
            <Text style={styles.streakText}>Day {streakDay}</Text>
          </TouchableOpacity>

          {/* Virtual Wallet */}
          <View style={styles.balanceBadge}>
            <Text style={styles.coinIcon}>🪙</Text>
            <Text style={styles.balanceText}>{balance.toLocaleString()}</Text>
          </View>
        </View>
      </View>

      {/* Toast Notification */}
      {toastMessage && (
        <TouchableOpacity style={styles.toast} onPress={clearToast}>
          <Text style={styles.toastText}>{toastMessage}</Text>
        </TouchableOpacity>
      )}

      {/* Main Content Pane */}
      <View style={styles.content}>
        {currentTab === 'roulette' && <RouletteTable />}
        {currentTab === 'slots' && <SlotMachine />}
        {currentTab === 'leaderboard' && <Leaderboard />}
      </View>

      {/* Bottom Navigation */}
      <View style={styles.bottomNav}>
        <TouchableOpacity
          style={[styles.navItem, currentTab === 'roulette' && styles.activeNavItem]}
          onPress={() => setCurrentTab('roulette')}
        >
          <Text style={styles.navIcon}>🎡</Text>
          <Text style={[styles.navLabel, currentTab === 'roulette' && styles.activeNavLabel]}>Roulette</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.navItem, currentTab === 'slots' && styles.activeNavItem]}
          onPress={() => setCurrentTab('slots')}
        >
          <Text style={styles.navIcon}>🎰</Text>
          <Text style={[styles.navLabel, currentTab === 'slots' && styles.activeNavLabel]}>Slots</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.navItem, currentTab === 'leaderboard' && styles.activeNavItem]}
          onPress={() => setCurrentTab('leaderboard')}
        >
          <Text style={styles.navIcon}>🏆</Text>
          <Text style={[styles.navLabel, currentTab === 'leaderboard' && styles.activeNavLabel]}>Rankings</Text>
        </TouchableOpacity>
      </View>

      {/* Daily Reward 24h Modal */}
      <DailyRewardModal
        visible={showDailyModal}
        onClose={() => setShowDailyModal(false)}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#0B0817' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#1F173D' },
  brandTitle: { fontSize: 20, fontWeight: '900', color: '#FFF', letterSpacing: 1 },
  brandAccent: { color: '#F59E0B' },
  subBrand: { fontSize: 10, color: '#9CA3AF' },
  headerActions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  streakBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#311F5E', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 20, borderWidth: 1, borderColor: '#7C3AED' },
  streakIcon: { fontSize: 13, marginRight: 4 },
  streakText: { color: '#E9D5FF', fontWeight: '800', fontSize: 11 },
  balanceBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#1A1230', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 20, borderWidth: 1, borderColor: '#F59E0B' },
  coinIcon: { fontSize: 14, marginRight: 4 },
  balanceText: { color: '#FCD34D', fontWeight: '900', fontSize: 13 },
  toast: { backgroundColor: '#10B981', padding: 10, marginHorizontal: 16, marginTop: 8, borderRadius: 10, alignItems: 'center' },
  toastText: { color: '#FFF', fontWeight: '800', fontSize: 12 },
  content: { flex: 1, paddingHorizontal: 12 },
  bottomNav: { flexDirection: 'row', height: 65, backgroundColor: '#120A28', borderTopWidth: 1, borderTopColor: '#24194D', justifyContent: 'space-around', alignItems: 'center' },
  navItem: { alignItems: 'center', padding: 8 },
  activeNavItem: { borderTopWidth: 2, borderTopColor: '#F59E0B' },
  navIcon: { fontSize: 20 },
  navLabel: { fontSize: 11, color: '#9CA3AF', fontWeight: '600', marginTop: 2 },
  activeNavLabel: { color: '#F59E0B', fontWeight: '800' }
});
