import { io } from 'socket.io-client';

const SERVER_URL = process.env.EXPO_PUBLIC_SERVER_URL || 'http://localhost:3001';

class SocketService {
  constructor() {
    this.socket = null;
    this.listeners = new Map();
  }

  connect(userId, userName) {
    if (this.socket && this.socket.connected) return;

    this.socket = io(SERVER_URL, {
      transports: ['websocket'],
      reconnection: true,
      reconnectionAttempts: 10,
      reconnectionDelay: 1000
    });

    this.socket.on('connect', () => {
      console.log('[SocketClient] Connected with ID:', this.socket.id);
      this.joinRoom('roulette-main', userId, userName);
      this.joinRoom('coinflip-main', userId, userName);
    });

    this.socket.on('disconnect', (reason) => {
      console.log('[SocketClient] Disconnected:', reason);
    });

    this.socket.on('connect_error', (error) => {
      console.warn('[SocketClient] Connection error:', error.message);
    });
  }

  joinRoom(roomId, userId, userName) {
    if (!this.socket) return;
    this.socket.emit('join_room', { roomId, userId, userName });
  }

  placeRouletteBet(betData) {
    if (!this.socket) return;
    this.socket.emit('place_roulette_bet', betData);
  }

  placeCoinflipBet(betData) {
    if (!this.socket) return;
    this.socket.emit('place_coinflip_bet', betData);
  }

  on(event, callback) {
    if (!this.socket) return;
    this.socket.on(event, callback);
  }

  off(event) {
    if (!this.socket) return;
    this.socket.off(event);
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export const socketService = new SocketService();
export default socketService;
