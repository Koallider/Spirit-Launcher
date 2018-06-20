package ru.ryakovlev.spiritlauncher.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import ru.ryakovlev.spiritlauncher.domain.HomeScreenIcon

/**
 * Created by roma on 05.06.2018.
 */

@Dao
interface HomeScreenIconDao {

    @Query("SELECT * from homeScreenIcon")
    fun getAll(): LiveData<List<HomeScreenIcon>>

    @Insert(onConflict = REPLACE)
    fun insert(icon: HomeScreenIcon)

    @Update(onConflict = REPLACE)
    fun update(icon: HomeScreenIcon)

    @Query("DELETE from homeScreenIcon")
    fun deleteAll()

    @Delete
    fun delete(icon: HomeScreenIcon)

    @Transaction
    fun deleteAndInsert(toDelete: HomeScreenIcon, toInsert: HomeScreenIcon){
        delete(toDelete)
        insert(toInsert)
    }
}