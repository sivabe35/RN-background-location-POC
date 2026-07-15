// TypeScript wrapper for the native background location modules
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { BackgroundLocationModule } = NativeModules as any;
const emitter = new NativeEventEmitter(BackgroundLocationModule);

export type StartOptions = {
  intervalMs?: number; // desired emit interval
  fastestIntervalMs?: number; // android fused
  distanceMeters?: number;
  accuracy?: 'high' | 'balanced' | 'low';
  notification?: { title?: string; text?: string };
};

export function startTracking(options: StartOptions = {}) {
  return BackgroundLocationModule.startTracking(options || {});
}

export function stopTracking() {
  return BackgroundLocationModule.stopTracking();
}

export function updateInterval(intervalMs: number) {
  return BackgroundLocationModule.updateInterval({ intervalMs });
}

export function onLocation(cb: (loc: any) => void) {
  const sub = emitter.addListener('BackgroundLocation', cb);
  return () => sub.remove();
}
