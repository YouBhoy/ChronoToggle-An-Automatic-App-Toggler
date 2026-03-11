package com.chronotoggle.data.db

import androidx.room.TypeConverter
import com.chronotoggle.data.model.SettingType

class Converters {

    @TypeConverter
    fun fromSettingType(value: SettingType): String = value.name

    @TypeConverter
    fun toSettingType(value: String): SettingType = SettingType.valueOf(value)
}
