import Foundation
import CoreLocation
import React

@objc(BackgroundLocationManager)
class BackgroundLocationManager: RCTEventEmitter, CLLocationManagerDelegate {
  private let locationManager = CLLocationManager()
  private var lastEmit: Date?
  private var intervalMs: Int = 15000

  override init() {
    super.init()
    locationManager.delegate = self
    locationManager.desiredAccuracy = kCLLocationAccuracyBest
    locationManager.allowsBackgroundLocationUpdates = true
    locationManager.pausesLocationUpdatesAutomatically = false
  }

  @objc
  func startTracking(_ options: NSDictionary) {
    if CLLocationManager.authorizationStatus() == .notDetermined {
      locationManager.requestAlwaysAuthorization()
    }
    if let ms = options["intervalMs"] as? Int { intervalMs = ms }
    locationManager.startUpdatingLocation()
  }

  @objc
  func updateInterval(_ options: NSDictionary) {
    if let ms = options["intervalMs"] as? Int { intervalMs = ms }
  }

  @objc
  func stopTracking() {
    locationManager.stopUpdatingLocation()
  }

  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.last else { return }
    let now = Date()
    if let last = lastEmit, now.timeIntervalSince(last) * 1000.0 < Double(intervalMs) {
      return
    }
    lastEmit = now
    let payload: [String: Any] = ["latitude": loc.coordinate.latitude, "longitude": loc.coordinate.longitude, "accuracy": loc.horizontalAccuracy, "timestamp": loc.timestamp.timeIntervalSince1970 * 1000]
    sendEvent(withName: "BackgroundLocation", body: payload)
  }

  override func supportedEvents() -> [String]! {
    return ["BackgroundLocation"]
  }
}
