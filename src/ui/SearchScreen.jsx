import { useState, useMemo } from 'react';
import { s, storeBadgeStyle, colors } from './styles';

const categories = ['Tümü', 'Süt ürünleri', 'Et', 'Fırın'];

// Map category names to product.category values from products.json
const CATEGORY_MAP = {
  'Süt ürünleri': 'dairy',
  'Et':           'meat',
  'Fırın':        'bakery',
};

export default function SearchScreen({
  products = [],
  stores = [],
  stockItems = [],
  basket = [],
  addItem,
  onBasket,
  onVariant,
}) {
  const [activeCategory, setActiveCategory] = useState('Tümü');
  const [query, setQuery] = useState('');

  // Filter products by search query + active category
  const filtered = useMemo(() => {
    let list = products;

    if (query.trim()) {
      const q = query.toLowerCase();
      list = list.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.brand?.toLowerCase().includes(q)
      );
    }

    if (activeCategory !== 'Tümü') {
      const cat = CATEGORY_MAP[activeCategory];
      if (cat) list = list.filter(p => p.category === cat);
    }

    return list;
  }, [products, query, activeCategory]);

  const basketCount = basket.reduce((sum, i) => sum + i.qty, 0);

  const handleAdd = (product) => {
    // If product has variants, open VariantPopup
    if (product.variants?.length > 1) {
      onVariant?.(product);
      return;
    }
    // Otherwise add directly with first variant (or no variant)
    addItem?.(product, product.variants?.[0] ?? '');
  };

  return (
    <div style={s.phone}>
      {/* Top Bar */}
      <div style={s.topbar}>
        <div>
          <div style={s.tbTitle}>Smart Market</div>
          <div style={s.tbSub}>Ataşehir</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span
            style={{ fontSize: 19, color: '#a8d8a8', cursor: 'pointer', position: 'relative' }}
            onClick={onBasket}
          >
            🛒
            {basketCount > 0 && (
              <span style={{
                position: 'absolute',
                top: -6, right: -8,
                background: colors.darkGreen,
                color: '#fff',
                borderRadius: '50%',
                fontSize: 9,
                width: 15, height: 15,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                {basketCount}
              </span>
            )}
          </span>
        </div>
      </div>

      {/* Body */}
      <div style={s.body}>
        {/* Search Bar — now functional */}
        <div style={{ ...s.searchBar, padding: 0, overflow: 'hidden' }}>
          <span style={{ fontSize: 14, color: colors.textPale, paddingLeft: 10 }}>🔍</span>
          <input
            type="text"
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Ürün ara..."
            style={{
              border: 'none',
              outline: 'none',
              background: 'transparent',
              fontSize: 13,
              color: colors.textDark,
              flex: 1,
              padding: '8px 10px 8px 6px',
            }}
          />
          {query.length > 0 && (
            <span
              onClick={() => setQuery('')}
              style={{ fontSize: 13, color: colors.textPale, paddingRight: 10, cursor: 'pointer' }}
            >
              ✕
            </span>
          )}
        </div>

        {/* Category Chips — now functional */}
        <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap', marginBottom: 10 }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              style={activeCategory === cat ? s.chipOn : { ...s.chip, border: '1px solid #b8d8b0' }}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Product Grid */}
        {filtered.length === 0 ? (
          <div style={{ textAlign: 'center', color: colors.textLight, marginTop: 30, fontSize: 13 }}>
            Ürün bulunamadı
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 7, marginBottom: 9 }}>
            {filtered.map((p) => (
              <div key={p.id} style={s.pcard}>
                <div style={s.pimg}>
                  <span style={{ fontSize: 28 }}>{p.icon ?? '🛍️'}</span>
                </div>
                <div style={s.pinfo}>
                  <div style={s.pname}>{p.name}</div>
                  {p.brand && (
                    <div style={{ fontSize: 9, color: colors.textLight, marginBottom: 1 }}>{p.brand}</div>
                  )}
                  <div style={s.pprice}>{p.price} TL</div>
                  <div style={s.ppriceSub}>den başlayan</div>
                  <span style={storeBadgeStyle(p.store)}>{p.store}</span>

                  {/* Add to basket button */}
                  <button
                    onClick={() => handleAdd(p)}
                    style={{
                      marginTop: 6,
                      width: '100%',
                      padding: '4px 0',
                      background: colors.darkGreen,
                      color: '#e8f5e3',
                      border: 'none',
                      borderRadius: 6,
                      fontSize: 10,
                      cursor: 'pointer',
                    }}
                  >
                    {p.variants?.length > 1 ? 'Seçenekler' : '+ Sepete ekle'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Basket Button — dynamic count */}
        <button style={s.greenBtn} onClick={onBasket}>
          🧺 Sepetim ({basketCount})
        </button>
      </div>

      {/* Bottom Nav */}
      <div style={s.bnav}>
        <div style={s.navItem}>
          <span style={{ fontSize: 18, color: colors.darkGreen }}>🔍</span>
          <span style={s.navLabelOn}>Ara</span>
        </div>
        <div style={{ ...s.navItem, cursor: 'pointer' }} onClick={onBasket}>
          <span style={{ fontSize: 18, color: colors.textPale }}>🧺</span>
          <span style={s.navLabel}>Sepet</span>
        </div>
        <div style={s.navItem}>
          <span style={{ fontSize: 18, color: colors.textPale }}>🗺️</span>
          <span style={s.navLabel}>Rota</span>
        </div>
      </div>
    </div>
  );
}