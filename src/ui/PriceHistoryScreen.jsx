import { s, colors } from './styles';

const chartPoints = [
  { x: 55,  y: 22, label: '129.90', period: 'H1' },
  { x: 125, y: 54, label: '82.00',  period: 'H2' },
  { x: 195, y: 50, label: '85.00',  period: 'H3' },
  { x: 265, y: 48, label: '89.00',  period: 'H4' },
];

const polylinePoints = chartPoints.map((p) => `${p.x},${p.y}`).join(' ');

const summaryCards = [
  { label: 'En ucuz',   period: 'H2', value: '82 TL',     bg: '#e8f5e3', border: '#b8d8a0', labelColor: '#3b7a45', periodColor: '#7ab87f', valueColor: '#1a4d24' },
  { label: 'En pahalı', period: 'H1', value: '129.90 TL', bg: '#fce8e8', border: '#f5b8b8', labelColor: '#c62828', periodColor: '#e57373', valueColor: '#c62828' },
  { label: 'Güncel H4', period: 'H4', value: '89 TL',     bg: '#f7f9f5', border: colors.borderGreen, labelColor: '#7ab87f', periodColor: '#7ab87f', valueColor: colors.textDark },
];

export default function PriceHistoryScreen({ onBack }) {
  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <button onClick={onBack} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a8d8a8', fontSize: 17 }}>←</button>
          <div style={s.tbTitle}>Fiyat geçmişi</div>
        </div>
        <span style={{ fontSize: 11, color: colors.textLight }}>H1 → H4</span>
      </div>

      {/* Body */}
      <div style={s.body}>
        {/* Product selector */}
        <div style={{
          background: colors.bgGreen,
          borderRadius: 8,
          padding: '6px 12px',
          display: 'inline-flex',
          alignItems: 'center',
          gap: 6,
          marginBottom: 10,
          border: `1px solid ${colors.borderGreen}`,
        }}>
          <span style={{ fontSize: 12, color: colors.textDark }}>Beyaz Peynir — Sütaş (SOK)</span>
          <span style={{ fontSize: 12, color: colors.darkGreen }}>▾</span>
        </div>

        {/* Chart card */}
        <div style={{ ...s.bcard, padding: '10px 12px' }}>
          <svg viewBox="0 0 300 85" style={{ width: '100%', height: 85 }}>
            {/* Axes */}
            <line x1="28" y1="8"  x2="28"  y2="72" stroke={colors.borderGreen} strokeWidth=".5" />
            <line x1="28" y1="72" x2="282" y2="72" stroke={colors.borderGreen} strokeWidth=".5" />
            {/* Grid lines */}
            {[20, 40, 60].map((y) => (
              <line key={y} x1="28" y1={y} x2="282" y2={y} stroke={colors.bgGreen} strokeWidth=".5" />
            ))}
            {/* Line */}
            <polyline points={polylinePoints} stroke={colors.darkGreen} strokeWidth="2" fill="none" />
            {/* Points + labels */}
            {chartPoints.map((p) => (
              <g key={p.period}>
                <circle cx={p.x} cy={p.y} r="3.5" fill={colors.darkGreen} />
                <text x={p.x} y={p.y - 5} fontSize="8" fill={colors.darkGreen} textAnchor="middle">{p.label}</text>
                <text x={p.x} y="80" fontSize="7" fill={colors.textLight} textAnchor="middle">{p.period}</text>
              </g>
            ))}
          </svg>
        </div>

        {/* Summary cards */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 7, marginTop: 9 }}>
          {summaryCards.map((card) => (
            <div
              key={card.label}
              style={{
                borderRadius: 8,
                padding: 7,
                textAlign: 'center',
                background: card.bg,
                border: `1px solid ${card.border}`,
              }}
            >
              <div style={{ fontSize: 9, color: card.labelColor, marginBottom: 2 }}>{card.label}</div>
              <div style={{ fontSize: 10, color: card.periodColor }}>{card.period}</div>
              <div style={{ fontSize: 15, fontWeight: 500, color: card.valueColor }}>{card.value}</div>
            </div>
          ))}
        </div>

        <div style={{ height: 9 }} />
        <button style={{ ...s.outlineBtn, marginBottom: 0 }} onClick={onBack}>← Sepete dön</button>
      </div>

      {/* Bottom Nav */}
      <div style={s.bnav}>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.textPale }}>🔍</span><span style={s.navLabel}>Ara</span></div>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.darkGreen }}>🧺</span><span style={s.navLabelOn}>Sepet</span></div>
        <div style={s.navItem}><span style={{ fontSize: 18, color: colors.textPale }}>🗺️</span><span style={s.navLabel}>Rota</span></div>
      </div>
    </div>
  );
}
