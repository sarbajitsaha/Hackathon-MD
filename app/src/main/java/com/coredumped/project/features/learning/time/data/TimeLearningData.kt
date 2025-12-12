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
        DailyRoutineItem("Wake Up", com.coredumped.project.R.drawable.wake_up, TimePeriod.Morning, com.coredumped.project.R.drawable.clock_icon, "It is morning. We wake up."),
        DailyRoutineItem("Brush Teeth", com.coredumped.project.R.drawable.brush_teeth, TimePeriod.Morning, com.coredumped.project.R.drawable.clock_icon, "It is morning. I brush my teeth."),
        DailyRoutineItem("Wear ID", com.coredumped.project.R.drawable.wear_id, TimePeriod.Morning, com.coredumped.project.R.drawable.clock_icon, "It is morning. I wear my ID."),
        DailyRoutineItem("Go to School", com.coredumped.project.R.drawable.go_to_school, TimePeriod.Morning, com.coredumped.project.R.drawable.clock_icon, "It is morning. I go to school."),
        DailyRoutineItem("Eat Lunch", com.coredumped.project.R.drawable.eat_lunch, TimePeriod.Afternoon, com.coredumped.project.R.drawable.clock_icon, "It is afternoon. I eat lunch."),
        DailyRoutineItem("Socialize / Play", com.coredumped.project.R.drawable.play, TimePeriod.Evening, com.coredumped.project.R.drawable.clock_icon, "It is evening. I play with friends."),
        DailyRoutineItem("Sleep", com.coredumped.project.R.drawable.sleep, TimePeriod.Night, com.coredumped.project.R.drawable.clock_icon, "It is night. I sleep.")
    )
}
