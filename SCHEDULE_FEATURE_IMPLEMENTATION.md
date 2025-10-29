# Schedule Feature Implementation Guide

## Overview
This document outlines the complete implementation of the schedule feature for MovieFlix_App. The feature allows users to schedule movies/series that are in their watchlist or favorites, displaying a schedule icon on movie posters.

## Features Implemented

### 1. **Schedule Button**
- Added next to the favorite button in movie details
- Only visible when a movie is in watchlist OR favorites
- Shows calendar icon when not scheduled
- Shows checkmark icon when scheduled
- Click to schedule: Opens date/time picker
- Click again to remove schedule

### 2. **Schedule Icon on Posters**
- Appears in the **top-left corner** of movie posters
- Shows for all scheduled movies across the app
- Visible in:
  - Home feed horizontal lists
  - Watchlist screen
  - Favorites screen
  - Recommendations list

### 3. **Rating Badge Position**
- Remains in the **top-right corner** (existing functionality)
- No changes to rating display

## Files Created

### Database Layer
1. **ScheduledEntity.kt** - Database entity for scheduled movies
   - Location: `data/local_storage/entity/ScheduledEntity.kt`
   - Fields: `id`, `movieResult`, `scheduledDate`

2. **ScheduledRepository.kt** - Repository interface
   - Location: `domain/repository/ScheduledRepository.kt`

3. **ScheduledRepositoryImpl.kt** - Repository implementation
   - Location: `data/repository/ScheduledRepositoryImpl.kt`

### Use Cases & ViewModel
4. **ScheduledMovies.kt** - Use case for scheduled movies
   - Location: `domain/usecases/ScheduledMovies.kt`

5. **ScheduledViewModel.kt** - ViewModel for schedule operations
   - Location: `presentation/viewmodels/ScheduledViewModel.kt`

### UI Components
6. **ScheduleDateTimeDialog.kt** - Date/time picker dialog
   - Location: `presentation/movie_details/ScheduleDateTimeDialog.kt`
   - Shows date picker first, then time picker
   - Validates future date/time selection

## Files Modified

### Database Configuration
1. **Constants.kt** - Added `SCHEDULED_TABLE_NAME` constant
2. **MovieDao.kt** - Added DAO methods for scheduled movies:
   - `insertScheduledMovie()`
   - `deleteScheduledMovie()`
   - `getAllScheduledMovies()`
   - `getScheduledMovieById()`

3. **LocalDataSource.kt** - Added local data source methods
4. **MovieDatabase.kt** - Updated database version to 6, added ScheduledEntity
5. **ViewUtils.kt** - Added `MIGRATION_5_6` for database migration
6. **DatabaseModule.kt** - Added dependency injection for ScheduledRepository

### UI Layouts
7. **fragment_movie_details.xml** - Added schedule button next to favorite button
8. **item_list.xml** - Added schedule icon overlay (top-left corner)
9. **item_small_list.xml** - Added schedule icon overlay (top-left corner)

### Adapters
10. **HorizontalAdapter.kt** - Added schedule icon display logic
11. **FavAdapters.kt** - Added schedule icon display logic
12. **WatchListAdapter.kt** - Added schedule icon display logic
13. **RecommendationAdapter.kt** - Added schedule icon display logic

### Fragment
14. **MovieDetailsFragment.kt** - Major updates:
    - Added `ScheduledViewModel` injection
    - Added schedule state tracking (`isScheduled`, `currentScheduledDate`)
    - Added schedule button click handler
    - Added scheduled movies observer
    - Added `updateScheduleButtonIcon()` method
    - Added `updateScheduleButtonVisibility()` method
    - Schedule button shows/hides based on watchlist/favorite status

## How It Works

### User Flow
1. **User adds movie to watchlist or favorites**
   - Schedule button becomes visible

2. **User clicks schedule button**
   - Date picker dialog appears
   - User selects date
   - Time picker dialog appears
   - User selects time
   - Movie is scheduled
   - Toast shows: "Scheduled for [date] at [time]"
   - Schedule button icon changes to checkmark

3. **Schedule icon appears on poster**
   - Small calendar icon in top-left corner
   - Shows across all movie lists

4. **User clicks schedule button again**
   - Schedule is removed
   - Toast shows: "Schedule removed"
   - Icon reverts to calendar

5. **User removes from watchlist/favorites**
   - Schedule button hides automatically
   - Any existing schedule remains in database

### Data Flow
```
MovieDetailsFragment
    ↓
ScheduledViewModel
    ↓
ScheduledMovies (UseCase)
    ↓
ScheduledRepository
    ↓
LocalDataSource
    ↓
MovieDao
    ↓
Room Database (scheduled_movies_table)
```

### Adapter Updates
```
Observer (scheduled movies) → ScheduledViewModel
    ↓
Update scheduledMovieIds Set
    ↓
Adapter.updateScheduledMovies(scheduledIds)
    ↓
notifyDataSetChanged()
    ↓
ViewHolder checks if movie.id in scheduledMovieIds
    ↓
Show/hide schedule icon
```

## Database Schema

### scheduled_movies_table
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER (PK) | Movie/Series ID |
| movieResult | TEXT | Serialized MovieResult object |
| scheduledDate | INTEGER | Timestamp in milliseconds |

## Icon Resources Used
- **Schedule button (not scheduled)**: `baseline_calendar_month_24.xml`
- **Schedule button (scheduled)**: `baseline_done_all_24.xml`
- **Poster overlay**: `baseline_calendar_month_24.xml` with background

## Styling Details
- Schedule button: Same style as watchlist/favorite buttons
- Schedule icon on poster:
  - Size: `@dimen/_16sdp` x `@dimen/_16sdp`
  - Position: Top-left corner, 4dp margin
  - Background: `@drawable/rating_bg` (semi-transparent black)
  - Padding: `@dimen/_2sdp`
  - Tint: White

## Testing Checklist
- [ ] Add movie to watchlist → Schedule button appears
- [ ] Add movie to favorites → Schedule button appears
- [ ] Click schedule → Date picker appears
- [ ] Select future date/time → Schedule saved, icon updates
- [ ] Scheduled movie shows icon on poster (all lists)
- [ ] Click scheduled button → Remove schedule
- [ ] Remove from watchlist/fav → Button hides
- [ ] Schedule persists across app restarts
- [ ] Database migration from v5 to v6 works

## Notes for Future Development
1. **Notifications**: Consider adding AlarmManager/WorkManager for scheduled reminders
2. **Schedule Management Screen**: Create a dedicated screen to view all scheduled movies
3. **Edit Schedule**: Allow users to change scheduled date/time without removing
4. **Schedule Filters**: Filter movies by "scheduled today", "scheduled this week", etc.
5. **Calendar Integration**: Export schedules to device calendar

## Migration Notes
- Database version updated from 5 to 6
- New table `scheduled_movies_table` created
- Existing data preserved
- No data loss during migration

## Dependencies
No new external dependencies were added. The implementation uses existing libraries:
- Room (database)
- LiveData (observers)
- Hilt (dependency injection)
- Material Components (UI)

## Performance Considerations
- Schedule icon visibility is computed efficiently using a Set lookup (O(1))
- Observers only trigger when data changes
- No performance impact on existing functionality
- Database queries are optimized with proper indexing (primary key)

---

**Implementation Date**: October 28, 2025
**Database Version**: 6
**Status**: ✅ Complete
