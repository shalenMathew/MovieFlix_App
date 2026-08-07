# Walkthrough - Series Tracking / Currently Watching

I have implemented the "Series Tracking" feature which allows you to track TV shows, cache them for offline use, and mark episodes as watched.

## Key Changes

### 1. Data & Database
- Created `SeriesTrackingEntity`, `SeasonTrackingEntity`, and `EpisodeTrackingEntity` to store show data locally.
- Added `SeriesTrackingDao` to manage this data.
- Updated `MovieDatabase` to version 8 with a migration to create these new tables.
- Implemented `SeriesTrackingRepositoryImpl` which handles the "Track" logic by fetching and caching all seasons and episodes from the API.

### 2. Presentation
- **Library**: Added a 3rd tab named **"Tracking"** that displays all your currently tracked series.
- **Movie Details**: Added a **"Track Series"** option inside the "More Options" (three dots) bottom sheet for TV shows. Clicking it will trigger the caching process.
- **Episode Details**: Added a **"Mark as Watched"** button. When clicked, it updates the watched status in the database.
- **Home Screen**: Added a **"Continue Watching"** row at the very top of the feed that automatically shows your tracked series.

### 3. Offline Support
- Updated `MovieDetailsRepositoryImpl` to serve cached season and episode data if the show is tracked and there is no internet connection.
- Episodes in the list will now show a "Watched" checkmark if they have been marked as such.

## Verification Done
- [x] Database migration from v7 to v8.
- [x] Tracking/Untracking logic including full season/episode caching.
- [x] UI integration in Library, Home, and Details screens.
- [x] "Mark as Watched" persistence.
- [x] Basic offline data serving.

## Screenshots/UI Progress
- You can find the "Tracking" tab in your Library.
- Look for "Track Series" in the "More" menu on any TV show page.
- "Continue Watching" will appear on Home once you track at least one show.
