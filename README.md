# Dynamic Multilevel Caching System

## Overview
This project implements a dynamic multilevel caching system that supports multiple cache levels with different eviction policies (LRU, LFU). The system dynamically manages cache levels, handles cache misses, and simulates data fetching from memory.

## Key Design Decisions
- **Eviction Policies:** We use LinkedHashMap for LRU policy as it maintains access order, and PriorityQueue for LFU to track least frequently accessed elements.
- **Modular Design:** Each cache level is independent, and new levels can be added or removed dynamically.
  
## How to Run
1. Compile the code:
   ```bash
   javac Main.java

2. Run the code:
    java Main