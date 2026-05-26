import { useState } from 'react';
import { s, colors } from './styles';

const modes = [
  {
    id: 'cheapest',
    icon: '💰',
    label: 'En ucuz',
    desc: 'Fiyatı en düşük market kombinasyonu',
  },
  {
    id: 'single',
    icon: '🏪',
    label: 'Tek market',
    desc: 'Hepsini tek marketten al',
  },
  {
    id: 'nearest',
    icon: '🚶',
    label: 'En yakın',
    desc: 'En az yürüyüş, en az market',
  },
];

export default function ModePicker({ onConfirm }) {
  const [selected, setSelected] = useState('cheapest');

  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={s.tbTitle}>Sepetim</div>
      </div>

      {/* Dimmed basket behind sheet */}
      <div style={{ background: 'rgba(26,77,36,0.08)', padding: '11px 12px 0' }}>
        <div style={{ opacity: 0.25 }}>
          <div style={s.bcard}><div style={s.bcTitle}>Beyaz Peynir</div></div>
          <div style={s.bcard}><div style={s.bcTitle}>Kaşar Peynir</div></div>
        </div>

        {/* Bottom sheet */}
        <div style={{
          background: '#fff',
          borderRadius: '14px 14px 0 0',
          padding: '13px 12px',
          borderTop: `2px solid ${colors.darkGreen}`,
        }}>
          {/* Handle */}
          <div style={{ width: 36, height: 4, background: colors.borderGreen, borderRadius: 2, margin: '0 auto 11px' }} />

          <div style={{ fontSize: 14, fontWeight: 500, color: colors.textDark, marginBottom: 11 }}>
            Rota modu seçin
          </div>

          {modes.map((mode) => {
            const isOn = selected === mode.id;
            return (
              <div
                key={mode.id}
                onClick={() => setSelected(mode.id)}
                style={{
                  border: `1px solid ${isOn ? colors.darkGreen : colors.borderGreen}`,
                  borderRadius: 10,
                  padding: '10px 12px',
                  marginBottom: 7,
                  background: isOn ? '#eef7ee' : '#f7faf5',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <div>
                  <div style={{ fontSize: 13, fontWeight: 500, color: colors.textDark }}>
                    <span style={{ marginRight: 5 }}>{mode.icon}</span>
                    {mode.label}
                  </div>
                  <div style={{ fontSize: 11, color: colors.textLight, marginTop: 2 }}>{mode.desc}</div>
                </div>
                {isOn && <span style={{ color: colors.darkGreen, fontSize: 15 }}>✓</span>}
              </div>
            );
          })}

          <button style={{ ...s.greenBtn, marginTop: 9 }} onClick={() => onConfirm?.(selected)}>
            → Sonuçları göster
          </button>
        </div>
      </div>
    </div>
  );
}
