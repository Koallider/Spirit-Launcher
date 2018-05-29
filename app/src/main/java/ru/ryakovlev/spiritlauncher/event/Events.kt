package ru.ryakovlev.spiritlauncher.event

import ru.ryakovlev.spiritlauncher.domain.Shortcut

/**
 * Created by roma on 28.05.2018.
 */
class StartApplicationEvent(val applicationInfo: ru.ryakovlev.spiritlauncher.domain.ApplicationInfo)

class ShortcutEvent(val shortcut: Shortcut)