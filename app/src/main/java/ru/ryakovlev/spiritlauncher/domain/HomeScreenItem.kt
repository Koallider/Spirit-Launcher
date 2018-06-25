package ru.ryakovlev.spiritlauncher.domain

/**
 * Created by roma on 05.06.2018.
 */
interface HomeScreenItem {
    val id: Long
    val page: Int
    val x: Int
    val y: Int
    val width: Int
    val height: Int
}