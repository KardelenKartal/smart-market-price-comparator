import { useState } from 'react';
import { s, colors } from './styles';

export default function BasketScreen({ basket, updateQty, removeItem, selectStore, onCompareRoutes, onPriceHistory }) {
  const items = basket ?? [];

  const total = items.reduce((sum, item) => {
    const store = item.stores?.find((st) => st.name === item.selectedStore);
    return sum + (store?.price ?? 0) * item.qty;
  }, 0);

  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={s.tbTitle}>Sepetim</div>
        <span style={{ background: '#a8d8a8', color: colors.textDark, borderRadius: 10, padding: '2px 9px', fontSize: 11 }}>
          {items.length} ürün
        </span>
      </div>

      {/* Body */}
      <div style={s.body}>
        {items.length === 0 && (
          <div style={{ textAlign: 'center', color: colors.textLight, marginTop: 40, fontSize: 13 }}>
            Sepetiniz boş
          </div>
        )}

        {items.map((item) => {
          const chosen = item.stores?.find((st) => st.name === item.selectedStore);
          return (
            <div key={item.id} style={s.bcard}>
              {/* Item header */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 5 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={s.bcTitle}>{item.name}</span>
                  <span style={{ fontSize: 10, color: colors.textLight }}>{item.variant}</span>
                </div>
                {/* Remove button */}
                <button
                  onClick={() => removeItem(item.id)}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: colors.textLight,
                    fontSize: 16,
                    lineHeight: 1,
                    padding: '0 2px',
                  }}
                  title="Sepetten kaldır"
                >
                  ✕
                </button>
              </div>

              {/* Store chips */}
              <div style={{ display: 'flex', gap: 5, marginBottom: 6, flexWrap: 'wrap' }}>
                {item.stores?.map((st) => (
                  <div
                    key={st.name}
                    onClick={() => selectStore(item.id, st.name)}
                    style={{
                      border: `1px solid ${colors.borderGreen}`,
                      borderRadius: 7,
                      padding: '4px 7px',
                      fontSize: 10,
                      cursor: 'pointer',
                      textAlign: 'center',
                      minWidth: 44,
                      ...(item.selectedStore === st.name
                        ? { background: colors.darkGreen, color: '#e8f5e3', borderColor: colors.darkGreen }
                        : { background: '#f7faf5', color: colors.midGreen }),
                    }}
                  >
                    {st.name}
                    <br />
                    <span style={{ fontSize: 9 }}>{st.price} TL</span>
                  </div>
                ))}
              </div>

              {/* Qty row */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: 10, color: colors.textLight }}>
                  {item.selectedStore} · {chosen?.price} TL
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <button style={s.qbtn} onClick={() => updateQty(item.id, -1)}>−</button>
                  <span style={{ fontSize: 12, color: colors.darkGreen, minWidth: 14, textAlign: 'center' }}>{item.qty}</span>
                  <button style={s.qbtn} onClick={() => updateQty(item.id, +1)}>+</button>
                </div>
              </div>
            </div>
          );
        })}

        {/* Total */}
        <div style={s.totalBar}>
          <span style={{ fontSize: 12, color: colors.midGreen }}>Toplam</span>
          <span style={{ fontSize: 16, fontWeight: 500, color: colors.darkGreen }}>{total} TL</span>
        </div>

        <button style={s.greenBtn} onClick={onCompareRoutes}>
          🗺️ Rotaları karşılaştır
        </button>
        <button style={s.outlineBtn} onClick={onPriceHistory}>
          Fiyat geçmişi
        </button>
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