package com.shalenmathew.movieflix.presentation.movie_details

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import com.shalenmathew.movieflix.R
import java.util.Calendar

object ScheduleDateTimeDialog {

    fun show(context: Context, onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Detect if dark mode is enabled
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val themeId = if (isDarkMode) R.style.DatePickerThemeDark else R.style.DatePickerThemeLight

        // Create themed context wrapper
        val themedContext = ContextThemeWrapper(context, themeId)

        // Show Date Picker first with custom theme
        val datePickerDialog = DatePickerDialog(
            themedContext,
            themeId,
            { _, year, month, dayOfMonth ->
                // After date is selected, show Time Picker with custom theme
                val timePickerDialog = TimePickerDialog(
                    themedContext,
                    themeId,
                    { _, hourOfDay, minute ->
                        // Create calendar with selected date and time
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        
                        val scheduledTime = selectedCalendar.timeInMillis
                        
                        // Validate that selected time is in the future
                        if (scheduledTime > System.currentTimeMillis()) {
                            onDateTimeSelected(scheduledTime)
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Please select a future date and time",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    currentHour,
                    currentMinute,
                    false // 12-hour format
                )
                timePickerDialog.show()
            },
            currentYear,
            currentMonth,
            currentDay
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}
