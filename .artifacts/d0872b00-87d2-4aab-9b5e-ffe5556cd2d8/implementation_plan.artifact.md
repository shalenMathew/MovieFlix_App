# Fix "Last Watched" Text Instability in MovieDetailsFragment

The "Last watched" bookmark text in the details screen is unstable because its visibility logic is only triggered by one of three asynchronous data sources (Series Progress). If the Watchlist or Favorites status arrives later, the UI is not updated to reflect the change.

## Proposed Changes

### [MovieDetailsFragment](file:///E:/2. Coding (Android)/Apps2/MovieFlix/app/src/main/java/com/shalenmathew/movieflix/presentation/movie_details/MovieDetailsFragment.kt)

#### [MODIFY] [MovieDetailsFragment.kt](file:///E:/2. Coding (Android)/Apps2/MovieFlix/app/src/main/java/com/shalenmathew/movieflix/presentation/movie_details/MovieDetailsFragment.kt)
- Add a private property `currentSeriesProgress` to cache the tracking data.
- Implement a helper method `updateLastWatchedUI()` to centralize visibility logic.
- Update `WatchListViewModel` and `FavMovieViewModel` observers to call `updateLastWatchedUI()`.
- Update `SeriesTrackingViewModel` observer to update the cache and call `updateLastWatchedUI()`.

## Verification Plan

### Manual Verification
1. Open a TV show that is already tracked (has a last watched episode).
2. Add it to Favorites -> Verify "Last watched" text appears immediately.
3. Remove from Favorites (if not in Watchlist) -> Verify "Last watched" text disappears immediately.
4. Add to Watchlist -> Verify "Last watched" text appears immediately.
5. Close and reopen the app/fragment to verify the text appears correctly regardless of load order.
