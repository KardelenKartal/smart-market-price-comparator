import { useState } from 'react';
import { s, colors } from './styles';

export default function VariantPopup({ product, basket = [], addItem, updateQty, removeItem, onClose }) {
  const [selectedVariant, setSelectedVariant] = useState(null);

  // Use variants from the product prop, fall back to empty array
  const variants = product?.variants ?? [];

  const handleAddToBasket = () => {
    if (!selectedVariant) return;
    addItem?.(product, selectedVariant);
    onClose?.();
  };

  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div style={s.tbTitle}>Smart Market</div>
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a8d8a8', fontSize: 17 }}
        >
          ✕
        </button>
      </div>

      {/* Body */}
      <div style={s.body}>

        {/* Variant selector popup */}
        <div style={{ background: 'rgba(26,77,36,0.18)', borderRadius: 10, padding: 8, marginBottom: 12 }}>
          <div style={{ background: '#fff', border: `1px solid ${colors.borderGreen}`, borderRadius: 10, padding: '10px 12px' }}>
            
            {/* Popup header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <span style={{ fontSize: 13, fontWeight: 500, color: colors.textDark }}>
                {product?.name ?? 'Ürün'}
              </span>
              <button
                onClick={onClose}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: colors.textLight, fontSize: 14 }}
              >
                ✕
              </button>
            </div>

            <div style={{ fontSize: 11, color: colors.textLight, marginBottom: 6 }}>Varyant seçin</div>

            {/* Variant list */}
            {variants.map((v, i) => (
              <div
                key={v.id ?? i}
                onClick={() => setSelectedVariant(v.name)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '6px 4px',
                  borderBottom: i < variants.length - 1 ? `1px solid #eef5ec` : 'none',
                  cursor: 'pointer',
                  background: selectedVariant === v.name ? '#eef7ee' : 'transparent',
                  borderRadius: 4,
                }}
              >
                <span style={{ fontSize: 12, color: colors.textDark }}>{v.name}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: 12, fontWeight: 500, color: colors.darkGreen }}>{v.price} TL</span>
                  {selectedVariant === v.name && (
                    <span style={{ fontSize: 11, color: colors.darkGreen }}>✓</span>
                  )}
                </div>
              </div>
            ))}

            {/* Add button — disabled until a variant is selected */}
            <button
              onClick={handleAddToBasket}
              disabled={!selectedVariant}
              style={{
                marginTop: 10,
                width: '100%',
                padding: '7px 0',
                background: selectedVariant ? colors.darkGreen : '#c8e0c8',
                color: selectedVariant ? '#e8f5e3' : '#8aaa8a',
                border: 'none',
                borderRadius: 7,
                fontSize: 12,
                cursor: selectedVariant ? 'pointer' : 'not-allowed',
              }}
            >
              {selectedVariant ? `+ Sepete ekle — ${selectedVariant}` : 'Varyant seçin'}
            </button>
          </div>
        </div>

        {/* Current basket items (read-only preview from real basket) */}
        {basket.length > 0 && (
          <>
            <div style={{ fontSize: 11, color: colors.textLight, marginBottom: 6 }}>Sepetim</div>
            {basket.map((item, i) => (
              <div
                key={item.id}
                style={{
                  padding: '6px 0',
                  borderBottom: i < basket.length - 1 ? `1px solid #eef5ec` : 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <div>
                  <div style={{ fontSize: 12, color: colors.textDark }}>{item.name}</div>
                  <div style={{ fontSize: 10, color: colors.textLight }}>{item.variant}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <button style={s.qbtn} onClick={() => updateQty?.(item.id, -1)}>−</button>
                  <span style={{ fontSize: 12, color: colors.darkGreen, minWidth: 14, textAlign: 'center' }}>
                    {item.qty}
                  </span>
                  <button style={s.qbtn} onClick={() => updateQty?.(item.id, +1)}>+</button>
                </div>
              </div>
            ))}
          </>
        )}
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