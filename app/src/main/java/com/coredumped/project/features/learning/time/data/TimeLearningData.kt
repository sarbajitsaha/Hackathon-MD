package com.coredumped.project.features.learning.time.data

import androidx.annotation.DrawableRes

enum class TimePeriod { Morning, Afternoon, Evening, Night }

data class DailyRoutineItem(
    val name: String,
    @DrawableRes val activityImageRes: Int,
    val period: TimePeriod,
    @DrawableRes val timeIconRes: Int,
    val voiceoverText: String? = null
)

object TimeLearningData {
    // Placeholder drawable IDs assumed to exist; replace with real assets as available.
    // Using existing icons if available; otherwise reference generic ones.
    val items: List<DailyRoutineItem> = listOf(
        DailyRoutineItem("Wake Up", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Morning, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is morning. We wake up."),
        DailyRoutineItem("Brush Teeth", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Morning, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is morning. I brush my teeth."),
        DailyRoutineItem("Wear ID", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Morning, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is morning. I wear my ID."),
        DailyRoutineItem("Go to School", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Morning, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is morning. I go to school."),
        DailyRoutineItem("Eat Lunch", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Afternoon, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is afternoon. I eat lunch."),
        DailyRoutineItem("Socialize / Play", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Afternoon, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is afternoon. I play with friends."),
        DailyRoutineItem("Homework", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Evening, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is evening. I do homework."),
        DailyRoutineItem("Play at Home", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Evening, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is evening. I play at home."),
        DailyRoutineItem("Wash Hands/Face", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Night, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is night. I wash my hands and face."),
        DailyRoutineItem("Sleep", com.coredumped.project.R.drawable.ic_launcher_foreground, TimePeriod.Night, com.coredumped.project.R.drawable.ic_launcher_foreground, "It is night. I sleep.")
    )
}
