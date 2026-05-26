import { s, colors } from './styles';

const routes = [
  { id: 1, label: '1. öneri', name: 'SOK + A101',  price: 303, markets: 2, time: '7 dk',  dist: '0.6 km', top: true },
  { id: 2, label: null,       name: 'CF + A101',   price: 316, markets: 2, time: '11 dk', dist: '0.9 km', top: false },
  { id: 3, label: null,       name: 'BIM + A101',  price: 326, markets: 2, time: '9 dk',  dist: '0.7 km', top: false },
  { id: 4, label: null,       name: 'SOK + CF',    price: 341, markets: 2, time: '14 dk', dist: '1.1 km', top: false },
  { id: 5, label: null,       name: 'MIGROS tek',  price: 381, markets: 1, time: '5 dk',  dist: '0.3 km', top: false },
];

export default function RouteResults({ onBack, onShowMap }) {
  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <button onClick={onBack} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a8d8a8', fontSize: 17 }}>←</button>
          <div style={s.tbTitle}>Sonuçlar</div>
        </div>
        <span style={{ fontSize: 11, color: colors.textLight }}>En ucuz mod</span>
      </div>

      {/* Body */}
      <div style={s.body}>
        <div style={{ fontSize: 11, color: colors.textLight, marginBottom: 7 }}>En iyi 5 kombinasyon</div>

        {routes.map((route) => (
          <div
            key={route.id}
            style={{
              background: '#fff',
              border: route.top
                ? `1.5px solid ${colors.darkGreen}`
                : `1px solid ${colors.softBorder}`,
              borderRadius: 10,
              padding: '9px 11px',
              marginBottom: 7,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                {route.label && (
                  <div style={{ fontSize: 10, color: colors.darkGreen, fontWeight: 500, marginBottom: 2 }}>
                    {route.label}
                  </div>
                )}
                <div style={{ fontSize: 13, fontWeight: 500, color: colors.textDark }}>{route.name}</div>
              </div>
              <span style={{ fontSize: 13, fontWeight: 500, color: colors.darkGreen }}>{route.price} TL</span>
            </div>

            {/* Pills */}
            <div style={{ display: 'flex', gap: 5, marginTop: 5, flexWrap: 'wrap' }}>
              {[
                `🏪 ${route.markets} market`,
                `🚶 ${route.time}`,
                `📍 ${route.dist}`,
              ].map((pill) => (
                <span
                  key={pill}
                  style={{
                    background: '#eef7ee',
                    color: colors.midGreen,
                    borderRadius: 5,
                    padding: '2px 7px',
                    fontSize: 10,
                  }}
                >
                  {pill}
                </span>
              ))}
            </div>
          </div>
        ))}

        <button style={{ ...s.greenBtn, marginTop: 5 }} onClick={onShowMap}>
          🗺️ Haritada gör
        </button>
      </div>

      {/* Bottom Nav */}
      <div style={s.bnav}>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.textPale }}>🔍</span><span style={s.navLabel}>Ara</span></div>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.textPale }}>🧺</span><span style={s.navLabel}>Sepet</span></div>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.darkGreen }}>🗺️</span><span style={s.navLabelOn}>Rota</span></div>
      </div>
    </div>
  );
}
