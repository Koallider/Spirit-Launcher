package ru.ryakovlev.spiritlauncher.domain

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by roma on 05.06.2018.
 */
@Entity
data class HomeScreenIcon(
        override var x: Int,
        override var y: Int,
        val packageNameList: List<String>,
        var name: String = "") : HomeScreenItem {
    @PrimaryKey(autoGenerate = true) override var id: Long = 0
    override var width: Int = 1
    override var height: Int = 1

}