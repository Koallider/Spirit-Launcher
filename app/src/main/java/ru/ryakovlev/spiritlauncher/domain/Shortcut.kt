package ru.ryakovlev.spiritlauncher.domain

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable

/**
 * Created by roma on 29.05.2018.
 */
class Shortcut(val shortcutInfo: ShortcutInfo, var drawable: Drawable? = null)