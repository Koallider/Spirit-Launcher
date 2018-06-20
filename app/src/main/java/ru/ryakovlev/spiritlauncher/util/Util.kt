package ru.ryakovlev.spiritlauncher.util

/**
 * Created by roma on 09.06.2018.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() =
        if (all { it is T })
            this as List<T>
        else null