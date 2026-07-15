/* Example React Native app using the background location wrapper */
import React, { useEffect, useState } from 'react';
import { SafeAreaView, Button, Text, View, Slider, Platform } from 'react-native';
import * as BackgroundLocation from '../src/background';

export default function App() {
  const [running, setRunning] = useState(false);
  const [intervalMs, setIntervalMs] = useState(15000);
  const [last, setLast] = useState<any>(null);

  useEffect(() => {
    const unsub = BackgroundLocation.onLocation((loc) => {
      setLast(loc);
      console.log('loc', loc);
    });
    return () => unsub();
  }, []);

  const start = async () => {
    await BackgroundLocation.startTracking({ intervalMs, notification: { title: 'Tracking', text: 'Background location running' } });
    setRunning(true);
  };

  const stop = async () => {
    await BackgroundLocation.stopTracking();
    setRunning(false);
  };

  const update = async (ms: number) => {
    setIntervalMs(ms);
    await BackgroundLocation.updateInterval(ms);
  };

  return (
    <SafeAreaView style={{ flex: 1, padding: 16 }}>
      <View style={{ marginVertical: 8 }}>
        <Text>Interval: {intervalMs} ms</Text>
      </View>
      <View style={{ marginVertical: 8 }}>
        <Button title={running ? 'Stop' : 'Start'} onPress={running ? stop : start} />
      </View>
      <View style={{ marginVertical: 8 }}>
        <Text>Last location:</Text>
        <Text>{last ? JSON.stringify(last) : 'no data yet'}</Text>
      </View>
      <View>
        <Text>Adjust interval</Text>
        {/* Slider is removed from core RN in newer versions; if missing, use community slider. Here it's illustrative. */}
        {Platform.OS === 'android' || Platform.OS === 'ios' ? (
          // @ts-ignore
          <Slider
            minimumValue={5000}
            maximumValue={60000}
            step={5000}
            value={intervalMs}
            onValueChange={(val) => setIntervalMs(val)}
            onSlidingComplete={(val) => update(Math.round(val))}
          />
        ) : null}
      </View>
    </SafeAreaView>
  );
}
