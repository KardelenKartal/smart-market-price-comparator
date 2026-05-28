# Smart Market — Walkable Grocery Route Optimizer

A mobile-first supermarket comparison and route optimization application designed for real-world grocery shopping.

Smart Market helps users compare supermarket prices, build shopping baskets, and generate optimized walking routes between nearby market chains based on different shopping priorities such as:

- lowest total price,
- shortest walking distance,
- or minimizing the number of supermarket stops.

The project was developed as part of the Yeditepe University CSE344 Software Engineering course.

---

# Project Motivation

In large cities, the same grocery products are often sold at significantly different prices across supermarket chains.

Users who want to reduce shopping costs usually need to:

- manually compare multiple markets,
- check availability,
- calculate walking distances,
- and decide whether visiting multiple stores is actually worth it.

Most existing supermarket applications focus only on:

- online delivery,
- single-store shopping,
- or price comparison without physical route planning.

Smart Market was designed specifically for **in-person walkable grocery shopping**.

The application combines:

- supermarket price comparison,
- basket optimization,
- and route generation

into a single mobile-oriented workflow.

---

# Features

## Product Search

- Search products across multiple supermarket chains
- Turkish character normalization support (`ş/s`, `ı/i`, `ç/c`, etc.)
- Category-based filtering
- AI-assisted product search experimentation
- Variant comparison between markets

## Basket Management

- Add products to shopping basket
- Compare market-specific prices
- Quantity management
- Dynamic total price calculation

## Route Optimization

The system supports multiple optimization modes depending on user priorities.

### Cheapest Mode

Finds the lowest-cost supermarket combination for the selected basket, even if multiple stores are required.

### Single Market Mode

Selects a single supermarket chain to reduce checkout friction and avoid multiple stops.

### Shortest Distance Mode

Prioritizes minimum walking distance and fewer route transitions for faster in-person shopping.

## Interactive Map

- Leaflet-based map integration
- Walking route visualization
- Store marker rendering
- Distance estimation
- Multiple starting point support

---

# Screenshots

## Product Search

(Add screenshot here)

## Basket Screen

(Add screenshot here)

## Route Suggestions

(Add screenshot here)

## Interactive Map

(Add screenshot here)

---

# Technologies Used

| Technology | Purpose |
|---|---|
| React | Frontend framework |
| Vite | Build tool |
| Leaflet | Interactive map rendering |
| JavaScript (ES Modules) | Application logic |
| JSON | Dataset management |
| Gemini API | AI-assisted search experimentation |
| ESLint | Code quality |

---

# Project Architecture

```text
src/
├── data/
├── service/
├── ui/
└── main.jsx
```

## Service-Based Structure

The project separates:

- UI rendering,
- optimization logic,
- search handling,
- and geographic utilities

into independent service modules.

### searchService.js

Handles:
- product search,
- filtering,
- normalization,
- and category matching.

### optimizationService.js

Responsible for:
- basket optimization,
- supermarket comparison,
- and route recommendation logic.

### geoUtils.js

Provides:
- distance calculations,
- coordinate utilities,
- and geographic helper functions.

### routeDisplayService.js

Handles:
- map rendering,
- route visualization,
- and display formatting.

---

# Dataset

The project uses a normalized static JSON dataset constructed from publicly accessible supermarket price listings.

The dataset includes:
- product names,
- supermarket variants,
- pricing information,
- and store locations

around the Kayışdağı / Ataşehir region.

Supported supermarket chains currently include:
- A101
- BIM
- ŞOK
- Migros
- CarrefourSA

## Dataset & Store Distribution

The project combines real supermarket chain data with additional simulated branch locations to better evaluate route optimization behavior under different shopping scenarios.

The final dataset includes:
- 5 supermarket chains,
- multiple branch locations per chain,
- product variants,
- pricing information,
- and geographic positioning data.

To improve testing quality and route diversity, additional synthetic store locations were introduced alongside existing real-world branches in the Kayışdağı / Ataşehir region.

This approach allowed the optimization system to be evaluated under more realistic multi-market shopping conditions without relying entirely on live external APIs.

Earlier development experiments also explored stock confidence and dynamically synchronized pricing systems. However, because live supermarket data sources were inconsistent and difficult to maintain reliably within the project scope, the final prototype focuses primarily on route optimization, basket comparison, and usability.

---

# Technical Challenges

## Dataset Consistency

One major challenge was maintaining consistency between:
- static JSON datasets,
- AI-assisted search results,
- supermarket naming variations,
- and category mappings.

To reduce duplication, helper normalization functions and Turkish-language adaptations were implemented directly in the search logic instead of manually rewriting datasets.

## Turkish Search Normalization

Product searches required handling Turkish character variations to improve usability and search reliability.

## Mobile-First UX Decisions

During development, the project scope shifted toward a fully mobile-first experience.

Since users would realistically interact with the application while physically walking between supermarkets, desktop-oriented interactions were intentionally deprioritized.

## Route Visualization & Leaflet Integration

Implementing route rendering with Leaflet introduced state synchronization and map re-rendering issues, especially during route recalculation and mode switching.

## AI-Assisted Search Integration

The project experimented with Gemini-assisted search functionality to improve product discovery.

Because real-time supermarket APIs were unreliable or inaccessible, fallback search mechanisms were implemented to preserve usability when AI query limits were reached.

## Optimization Tradeoffs

One important design challenge was determining how much the system should prioritize:
- total price,
- walking distance,
- number of supermarkets,
- and checkout convenience.

Instead of forcing supermarket chain preferences, the application focuses on practical real-world shopping efficiency.

---

# Future Improvements

Potential future improvements include:
- real-time supermarket API integration,
- persistent user baskets,
- GPS-based live navigation,
- barcode scanning,
- dark mode,
- public transport routing,
- and AI-powered shopping recommendations.

---

# Installation

Clone the repository:

```bash
git clone https://github.com/KardelenKartal/smart-market-price-comparator.git
```

Install dependencies:

```bash
npm install
```

Run development server:

```bash
npm run dev
```

Build for production:

```bash
npm run build
```

---

# Team

## Software Development Team (CSE344)

- Kardelen Kartal
- Barış Getiren
- Çağatay Yürekli
- Lina Şahin
- Metehan Kanlı
- Selin Suna Kaya

## System Analysis & Industrial Engineering Collaboration (ISE402)

- Ahmet Faruk İhtiyar
- Başak Urhan
- Baturalp Çapa

The project was developed collaboratively within the CSE344 Software Engineering and ISE402 System Design courses at Yeditepe University.

---

# Academic Context

This project was developed for the Yeditepe University CSE344 Software Engineering course as part of a semester-long software engineering and system design project.