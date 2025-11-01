package com.shalenmathew.movieflix.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

const val USER_DATASTORE= "user_datastore"

private val Context.userDatastore:DataStore<Preferences> by preferencesDataStore(USER_DATASTORE) // createDataStore is a property we created and
// in simple words making Context to extend it in this way we made createDataStore a property of Context

val IS_INTRO_COMPLETED = booleanPreferencesKey("IS_INTRO_COMPLETED")

object DataStoreReference{

    suspend fun updateIntroCompleted(context:Context, value:Boolean=false){
        context.userDatastore.edit {
            it[IS_INTRO_COMPLETED] = value
        }
        // its a asynchronous so we need to add in suspend
    }

  fun isIntroCompleted(context: Context)=context.userDatastore.data.map {
        it[IS_INTRO_COMPLETED]?:false
      // datastore returns flow
    }
}