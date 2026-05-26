/**
 * optimizationService
 * Sepetteki urunler icin 3 farkli stratejide en iyi 5 alisveris senaryosunu uretir.
 * Java tarafindaki RouteOptimizer.java'nin birebir karsiligidir.
 */

import { haversine } from './geoUtils.js';


// ================= SABITLER =================

// dondurulecek senaryo sayisi
const RESULT_LIMIT = 5;

// urun sayisi esigi (top 3 -> top 2 gecisi)
const PRODUCT_THRESHOLD = 13;


// ================= STRATEJI ENUM =================

// Java'daki RouteStrategy enum karsiligi
export const RouteStrategy = Object.freeze({
    CHEAPEST:   'CHEAPEST',
    ONE_MARKET: 'ONE_MARKET',
    SHORTEST:   'SHORTEST',
});


// ================= ANA METOT =================

/**
 * Verilen sepet ve stratejiye gore en iyi 5 alisveris senaryosunu dondurur.
 *
 * @param {Array} basketItems     - sepetteki urunler: [{ stockItemId, quantity }]
 * @param {Array} stockItems      - tum stock kayitlari (JSON'dan)
 * @param {Array} storeLocations  - magaza konumlari (JSON'dan)
 * @param {number} startLat       - baslangic noktasi enlem
 * @param {number} startLon       - baslangic noktasi boylam
 * @param {string} strategy       - RouteStrategy degerlerinden biri
 * @returns {Array} RouteResult listesi (max 5)
 */
export function optimizeRoute(basketItems, stockItems, storeLocations, startLat, startLon, strategy) {

    // bos sepet kontrolu
    if (basketItems == null || basketItems.length === 0) {
        return [];
    }

    // strateji secimine gore farkli yol
    if (strategy === RouteStrategy.ONE_MARKET) {
        return findOneMarketScenarios(basketItems, stockItems, storeLocations, startLat, startLon);
    }

    // CHEAPEST ve SHORTEST icin ortak yol
    return findMultiMarketScenarios(basketItems, stockItems, storeLocations, startLat, startLon, strategy);
}


// ================= ONE MARKET MANTIGI =================

/**
 * Tek markette tum urunleri satan marketleri bulur.
 * Fiyata gore siralar, ilk 5'i dondurur.
 * Bulamazsa bos liste doner.
 *
 * Brand belirtildiyse o brand'in o magazada olup olmadigi da kontrol edilir.
 */
function findOneMarketScenarios(basketItems, stockItems, storeLocations, startLat, startLon) {

    const results = [];

    // her magaza icin kontrol et: bu magazada tum urunler (gerekirse brand'leriyle) var mi?
    const stockByStore = groupStockByStore(stockItems);

    let routeIdCounter = 1;

    for (const [storeId, storeStock] of stockByStore.entries()) {

        // sepetteki her urun bu magazada uygun bir brand ile var mi?
        let allFound = true;
        for (const bi of basketItems) {
            let found = false;
            for (const si of storeStock) {
                if (equalsIgnoreCase(si.productId, bi.stockItemId) && brandMatches(si, bi)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                allFound = false;
                break;
            }
        }

        if (!allFound) continue;

        // toplam fiyati hesapla
        const totalCost = calculateOneMarketCost(basketItems, storeStock);

        // mesafeyi hesapla (baslangic -> magaza)
        const loc = findLocationById(storeLocations, storeId);
        let distance = 0.0;
        if (loc != null) {
            distance = haversine(startLat, startLon, loc.latitude, loc.longitude);
        }

        // sonuc olustur
        const result = buildRouteResult(routeIdCounter++, distance, 1, totalCost);
        result.stores.push({ routeId: result.routeId, storeId: storeId });
        results.push(result);
    }

    // fiyata gore sirala
    results.sort((a, b) => a.totalCost - b.totalCost);

    // ilk 5'i dondur
    return results.length > RESULT_LIMIT ? results.slice(0, RESULT_LIMIT) : results;
}

/**
 * Tek market icin sepetin toplam fiyatini hesaplar.
 * Brand belirtilmisse o brand'in fiyati kullanilir.
 */
function calculateOneMarketCost(basketItems, storeStock) {
    let total = 0.0;

    for (const bi of basketItems) {
        for (const si of storeStock) {
            if (equalsIgnoreCase(si.productId, bi.stockItemId) && brandMatches(si, bi)) {
                total += getEffectivePrice(si) * bi.quantity;
                break;
            }
        }
    }

    return total;
}


// ================= CHEAPEST & SHORTEST MANTIGI =================

/**
 * Tum kombinasyonlari uretip stratejiye gore siralar.
 */
function findMultiMarketScenarios(basketItems, stockItems, storeLocations, startLat, startLon, strategy) {

    // top market limiti (urun sayisina gore)
    const topLimit = (basketItems.length <= PRODUCT_THRESHOLD) ? 3 : 2;

    // her urun icin top N market opsiyonlarini bul
    const productOptions = [];

    for (const bi of basketItems) {
        const options = findTopOptionsForProduct(
                bi, stockItems, storeLocations,
                startLat, startLon, strategy, topLimit);

        if (options.length === 0) {
            continue;
        }

        productOptions.push(options);
    }

    if (productOptions.length === 0) {
        return [];
    }

    // tum kombinasyonlari uret
    const combinations = generateCombinations(productOptions);

    // her kombinasyon icin RouteResult olustur
    const results = [];
    let routeIdCounter = 1;

    for (const combo of combinations) {
        const result = buildResultFromCombination(
                combo, basketItems, storeLocations, startLat, startLon, routeIdCounter++);

        if (result != null) {
            results.push(result);
        }
    }

    // stratejiye gore sirala
    if (strategy === RouteStrategy.CHEAPEST) {
        results.sort((a, b) => a.totalCost - b.totalCost);
    } else if (strategy === RouteStrategy.SHORTEST) {
        results.sort((a, b) => a.totalDistance - b.totalDistance);
    }

    return results.length > RESULT_LIMIT ? results.slice(0, RESULT_LIMIT) : results;
}

/**
 * Bir urun icin stratejiye gore top N market opsiyonunu dondurur.
 * basketItem.brand belirtildiyse sadece o brand'li stock kayitlari aday olur.
 */
function findTopOptionsForProduct(basketItem, stockItems, storeLocations, startLat, startLon, strategy, topLimit) {

    const productId = basketItem.stockItemId;

    const candidates = [];
    for (const si of stockItems) {
        if (si.productId != null && equalsIgnoreCase(si.productId, productId) && brandMatches(si, basketItem)) {
            candidates.push(si);
        }
    }

    if (strategy === RouteStrategy.CHEAPEST) {
        candidates.sort((a, b) => getEffectivePrice(a) - getEffectivePrice(b));
    } else if (strategy === RouteStrategy.SHORTEST) {
        candidates.sort((a, b) => {
            const distA = distanceToStore(a.storeId, storeLocations, startLat, startLon);
            const distB = distanceToStore(b.storeId, storeLocations, startLat, startLon);
            return distA - distB;
        });
    }

    return candidates.length > topLimit ? candidates.slice(0, topLimit) : candidates;
}

function distanceToStore(storeId, storeLocations, startLat, startLon) {
    const loc = findLocationById(storeLocations, storeId);
    if (loc == null) return Number.MAX_VALUE;
    return haversine(startLat, startLon, loc.latitude, loc.longitude);
}


// ================= KOMBINASYON URETIMI =================

/**
 * Kartezyen carpim - her urunun her secenegi icin tum kombinasyonlari uretir.
 */
function generateCombinations(productOptions) {
    let result = [[]];

    for (const options of productOptions) {
        const newResult = [];
        for (const existing of result) {
            for (const option of options) {
                const newCombo = [...existing, option];
                newResult.push(newCombo);
            }
        }
        result = newResult;
    }

    return result;
}


// ================= SONUC OLUSTURMA =================

/**
 * Bir kombinasyondan RouteResult olusturur.
 */
function buildResultFromCombination(combo, basketItems, storeLocations, startLat, startLon, routeId) {

    if (combo.length !== basketItems.length) return null;

    // toplam fiyat
    let totalCost = 0.0;
    for (let i = 0; i < combo.length; i++) {
        totalCost += getEffectivePrice(combo[i]) * basketItems[i].quantity;
    }

    // unique magaza id'leri (sirayi korumak icin Set)
    const uniqueStores = new Set();
    for (const si of combo) {
        uniqueStores.add(si.storeId);
    }

    // waypoints
    const waypoints = [];
    for (const storeId of uniqueStores) {
        const loc = findLocationById(storeLocations, storeId);
        if (loc != null) waypoints.push(loc);
    }

    // nearest neighbor ile mesafe
    const totalDistance = calculateNearestNeighborDistance(waypoints, startLat, startLon);

    const result = buildRouteResult(
            routeId,
            Math.round(totalDistance * 1000.0) / 1000.0,
            uniqueStores.size,
            Math.round(totalCost * 100.0) / 100.0);

    for (const storeId of uniqueStores) {
        result.stores.push({ routeId: routeId, storeId: storeId });
    }

    return result;
}

/**
 * Nearest-neighbor algoritmasi ile siralanmis toplam mesafeyi hesaplar.
 */
function calculateNearestNeighborDistance(waypoints, startLat, startLon) {
    if (waypoints.length === 0) return 0.0;

    const remaining = [...waypoints];
    let totalDist = 0.0;
    let curLat = startLat;
    let curLon = startLon;

    while (remaining.length > 0) {
        let nearest = null;
        let nearestIndex = -1;
        let minDist = Number.MAX_VALUE;

        for (let i = 0; i < remaining.length; i++) {
            const loc = remaining[i];
            const d = haversine(curLat, curLon, loc.latitude, loc.longitude);
            if (d < minDist) {
                minDist = d;
                nearest = loc;
                nearestIndex = i;
            }
        }

        if (nearest != null) {
            totalDist += minDist;
            curLat = nearest.latitude;
            curLon = nearest.longitude;
            remaining.splice(nearestIndex, 1);
        }
    }

    return totalDist;
}


// ================= YARDIMCI METOTLAR =================

// Java RouteResult constructor karsiligi
function buildRouteResult(routeId, totalDistance, storeCount, totalCost) {
    return {
        routeId:       routeId,
        totalDistance: totalDistance,
        storeCount:    storeCount,
        totalCost:     totalCost,
        stores:        [],
    };
}

// Java StockItem.getEffectivePrice() karsiligi
function getEffectivePrice(stockItem) {
    return (stockItem.discountedPrice != null) ? stockItem.discountedPrice : stockItem.currentPrice;
}

function findLocationById(locations, storeId) {
    if (locations == null || storeId == null) return null;
    for (const loc of locations) {
        if (equalsIgnoreCase(storeId, loc.storeId)) return loc;
    }
    return null;
}

function groupStockByStore(stockItems) {
    const map = new Map();
    if (stockItems == null) return map;

    for (const si of stockItems) {
        const sid = si.storeId;
        if (sid == null) continue;
        if (!map.has(sid)) {
            map.set(sid, []);
        }
        map.get(sid).push(si);
    }

    return map;
}

// Java String.equalsIgnoreCase karsiligi
function equalsIgnoreCase(a, b) {
    if (a == null || b == null) return false;
    return a.toLowerCase() === b.toLowerCase();
}

// Java Set.containsAll karsiligi
function containsAll(superset, subset) {
    for (const item of subset) {
        if (!superset.has(item)) return false;
    }
    return true;
}

/**
 * Loose mod brand karsilastirmasi.
 * basketItem.brand null/bos ise -> her brand kabul edilir (geriye uyumluluk).
 * Doluysa -> stockItem.brand ile case-insensitive karsilastirilir.
 */
function brandMatches(stockItem, basketItem) {
    if (basketItem == null || basketItem.brand == null || basketItem.brand === '') {
        return true;
    }
    if (stockItem.brand == null) return false;
    return equalsIgnoreCase(stockItem.brand, basketItem.brand);
}


// ================= UI ADAPTER =================

/**
 * UI tarafindaki sepet formatini optimizer'in bekledigi BasketItem[] formatina cevirir.
 *
 * UI item'i: { id, name, variant, qty, productId?, brand?, ... }
 * Optimizer: { basketId, stockItemId, quantity, brand? }
 *
 * Notlar:
 *  - UI item'inda productId yoksa item atlanir.
 *  - brand opsiyoneldir (loose mod): brand yoksa optimizer ayni productId
 *    altindaki tum marka/varyantlari aday kabul eder.
 */
export function basketToOptimizerInput(uiBasket) {
    if (uiBasket == null || uiBasket.length === 0) return [];

    const result = [];
    for (const item of uiBasket) {
        // productId yoksa atla - UI ekibinin doldurmasi bekleniyor
        if (item.productId == null) continue;

        result.push({
            basketId:    item.id != null ? item.id : 1,
            stockItemId: item.productId,
            quantity:    item.qty != null ? item.qty : 1,
            brand:       item.brand != null ? item.brand : null,
        });
    }
    return result;
}

/**
 * UI mode ID'sini Java strategy enum'una cevirir.
 * UI:        'cheapest' | 'single' | 'nearest'
 * Optimizer: CHEAPEST   | ONE_MARKET | SHORTEST
 */
export function uiModeToStrategy(uiMode) {
    switch (uiMode) {
        case 'cheapest': return RouteStrategy.CHEAPEST;
        case 'single':   return RouteStrategy.ONE_MARKET;
        case 'nearest':  return RouteStrategy.SHORTEST;
        default:         return RouteStrategy.CHEAPEST;
    }
}

/**
 * Optimizer ciktisi RouteResult'lari UI'in bekledigi gorsel formata cevirir.
 *
 * Eklenen alanlar:
 *   - name: "SOK + A101" gibi chain isimlerinin birlesimi
 *   - timeMin: yurume suresi (dakika)
 *   - markets: storeCount (kisa isim)
 *
 * Mevcut alanlar (routeId, totalCost, totalDistance, storeCount, stores) korunur.
 */
export function enrichResultsForUI(results, storeLocations) {
    if (results == null || results.length === 0) return [];

    const enriched = [];
    for (const r of results) {
        // chain isimlerini topla (storeId'den chain'i cikar)
        const chainNames = [];
        for (const rs of r.stores) {
            const chain = chainFromStoreId(rs.storeId, storeLocations);
            if (chain != null && !chainNames.includes(chain)) {
                chainNames.push(chain);
            }
        }

        // tek market ise "MIGROS tek" formati, coklu ise "SOK + A101"
        const name = (r.storeCount === 1 && chainNames.length === 1)
            ? `${chainNames[0]} tek`
            : chainNames.join(' + ');

        // yurume suresi: 5 km/h = 12 dk/km
        const timeMin = Math.round(r.totalDistance * 12);

        enriched.push({
            routeId:       r.routeId,
            name:          name,
            totalCost:     r.totalCost,
            totalDistance: r.totalDistance,
            storeCount:    r.storeCount,
            stores:        r.stores,
            timeMin:       timeMin,
        });
    }
    return enriched;
}

/**
 * Bir storeId'nin hangi chain'e ait oldugunu bulur.
 * Onceliki store_locations.json'daki chain field'i, yoksa storeId prefix'i.
 */
function chainFromStoreId(storeId, storeLocations) {
    if (storeId == null) return null;

    // Once storeLocations'ta arayalim - chain field'i orada var
    if (storeLocations != null) {
        for (const loc of storeLocations) {
            if (equalsIgnoreCase(loc.storeId, storeId) && loc.chain != null) {
                return loc.chain;
            }
        }
    }

    // Bulamazsak storeId'nin prefix'ini al (A101-01 -> A101)
    const dash = storeId.lastIndexOf('-');
    return dash > 0 ? storeId.substring(0, dash) : storeId;
}
