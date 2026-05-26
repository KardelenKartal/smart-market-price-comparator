import { useState } from 'react';
import SearchScreen       from './SearchScreen';
import VariantPopup       from './VariantPopup';
import BasketScreen       from './BasketScreen';
import ModePicker         from './ModePicker';
import RouteResults       from './RouteResults';
import MapScreen          from './MapScreen';
import PriceHistoryScreen from './PriceHistoryScreen';
import { appData }        from '../data';

const SCREENS = {
  SEARCH:        'search',
  VARIANT:       'variant',
  BASKET:        'basket',
  MODE_PICKER:   'mode_picker',
  ROUTE_RESULTS: 'route_results',
  MAP:           'map',
  PRICE_HISTORY: 'price_history',
};

export default function App() {
  const [screen, setScreen]           = useState(SCREENS.SEARCH);
  const [basket, setBasket]           = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);

  const nav = (s) => setScreen(s);

  // ── Basket operations ──────────────────────────────────────────
  const addItem = (product, variant) => {
    setBasket(prev => {
      const existing = prev.find(
        i => i.productId === product.id && i.variant === variant
      );
      if (existing) {
        return prev.map(i =>
          i.productId === product.id && i.variant === variant
            ? { ...i, qty: i.qty + 1 }
            : i
        );
      }
      return [...prev, {
        id:            Date.now(),
        productId:     product.id,
        brand:         product.brand,
        name:          product.name,
        variant:       variant,
        qty:           1,
        price:         product.price,
        selectedStore: product.stores?.[0]?.name ?? null,
        stores:        product.stores ?? [],
      }];
    });
  };

  const removeItem = (id) => {
    setBasket(prev => prev.filter(i => i.id !== id));
  };

  const updateQty = (id, delta) => {
    setBasket(prev =>
      prev.map(i =>
        i.id === id ? { ...i, qty: Math.max(0, i.qty + delta) } : i
      )
    );
  };

  const selectStore = (itemId, storeName) => {
    setBasket(prev =>
      prev.map(i =>
        i.id === itemId ? { ...i, selectedStore: storeName } : i
      )
    );
  };

  // ── Shared basket props (passed to every screen that needs them) ──
  const basketProps = { basket, addItem, removeItem, updateQty, selectStore };

  return (
    <div style={{ maxWidth: 320, margin: '0 auto', fontFamily: 'system-ui, sans-serif' }}>

      {screen === SCREENS.SEARCH && (
        <SearchScreen
          products={appData.products}
          stores={appData.stores}
          stockItems={appData.stockItems}
          storeLocations={appData.storeLocations}
          {...basketProps}
          onBasket={() => nav(SCREENS.BASKET)}
          onVariant={(product) => {
            setSelectedProduct(product);
            nav(SCREENS.VARIANT);
          }}
        />
      )}

      {screen === SCREENS.VARIANT && (
        <VariantPopup
          product={selectedProduct}
          products={appData.products}
          {...basketProps}
          onClose={() => nav(SCREENS.BASKET)}
        />
      )}

      {screen === SCREENS.BASKET && (
        <BasketScreen
          stores={appData.stores}
          stockItems={appData.stockItems}
          {...basketProps}
          onCompareRoutes={() => nav(SCREENS.MODE_PICKER)}
          onPriceHistory={() => nav(SCREENS.PRICE_HISTORY)}
        />
      )}

      {screen === SCREENS.MODE_PICKER && (
        <ModePicker
          startPoints={appData.startPoints}
          {...basketProps}
          onConfirm={() => nav(SCREENS.ROUTE_RESULTS)}
        />
      )}

      {screen === SCREENS.ROUTE_RESULTS && (
        <RouteResults
          stores={appData.stores}
          storeLocations={appData.storeLocations}
          startPoints={appData.startPoints}
          {...basketProps}
          onBack={() => nav(SCREENS.BASKET)}
          onShowMap={() => nav(SCREENS.MAP)}
        />
      )}

      {screen === SCREENS.MAP && (
        <MapScreen
          storeLocations={appData.storeLocations}
          {...basketProps}
          onBack={() => nav(SCREENS.ROUTE_RESULTS)}
        />
      )}

      {screen === SCREENS.PRICE_HISTORY && (
        <PriceHistoryScreen
          products={appData.products}
          {...basketProps}
          onBack={() => nav(SCREENS.BASKET)}
        />
      )}

      {/* Dev screen switcher */}
      <div style={{ maxWidth: '100%', margin: '0 auto', fontFamily: 'system-ui, sans-serif' }}>
        {Object.entries(SCREENS).map(([key, val]) => (
          <button
            key={key}
            onClick={() => nav(val)}
            style={{
              fontSize: 10,
              padding: '3px 8px',
              border: `1px solid #b8d8b0`,
              borderRadius: 6,
              background: screen === val ? '#1a4d24' : '#f7f9f5',
              color: screen === val ? '#e8f5e3' : '#3b7a45',
              cursor: 'pointer',
            }}
          >
            {key}
          </button>
        ))}
      </div>
    </div>
  );
}