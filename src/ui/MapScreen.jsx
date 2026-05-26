import { useRef, useEffect } from 'react';
import { s, colors } from './styles';
import { drawRoute, clearRoute } from '../service/routeDisplayService';
import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';

export default function MapScreen({ onBack, routeResult, storeLocations, startLat, startLon }) {
  const mapRef = useRef(null);
  const leafletMap = useRef(null);

  useEffect(() => {
    if (mapRef.current && !leafletMap.current) {
      leafletMap.current = L.map(mapRef.current).setView(
        [startLat || 40.978, startLon || 29.141], 14
      );
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
      }).addTo(leafletMap.current);
    }
  }, []);

  useEffect(() => {
    if (leafletMap.current && routeResult) {
      drawRoute(leafletMap.current, routeResult, storeLocations, startLat, startLon);
    }
  }, [routeResult]);

  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <button onClick={onBack} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a8d8a8', fontSize: 17 }}>←</button>
          <div style={s.tbTitle}>Harita / Rota</div>
        </div>
        <span style={{ fontSize: 11, color: colors.textLight }}>
          {routeResult ? routeResult.stores.map(s => s.storeId).join(' + ') : ''}
        </span>
      </div>

      {/* Body */}
      <div style={{ ...s.body, padding: 8 }}>
        <div style={{ fontSize: 11, color: colors.textLight, marginBottom: 5 }}>Başlangıç noktası:</div>

        {/* Map */}
        <div ref={mapRef} style={{ height: 250, width: '100%', borderRadius: 10, marginBottom: 8 }} />

        {/* Route info */}
        {routeResult && (
          <div style={s.bcard}>
            <div style={{ fontSize: 11, color: colors.textLight, marginBottom: 4 }}>Önerilen rota</div>
            <div style={{ fontSize: 13, color: colors.textDark, marginBottom: 5 }}>
              {routeResult.stores.map(s => s.storeId).join(' → ')}
            </div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              {[
                `📍 ${routeResult.totalDistance?.toFixed(2)} km`,
                `🚶 ${routeResult.timeMin || Math.round(routeResult.totalDistance * 12)} dk`,
                `💰 ${routeResult.totalCost} TL`
              ].map((pill) => (
                <span key={pill} style={{ background: '#eef7ee', color: colors.midGreen, borderRadius: 5, padding: '2px 7px', fontSize: 10 }}>
                  {pill}
                </span>
              ))}
            </div>
          </div>
        )}

        <button style={{ ...s.outlineBtn, marginBottom: 0 }} onClick={onBack}>← Sonuçlara dön</button>
      </div>
    </div>
  );
}