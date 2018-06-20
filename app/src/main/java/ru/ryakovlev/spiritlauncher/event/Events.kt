package ru.ryakovlev.spiritlauncher.event

import ru.ryakovlev.spiritlauncher.domain.Shortcut

/**
 * Created by roma on 28.05.2018.
 */
class StartApplicationEvent(val packageName: String)

class ShortcutEvent(val shortcut: Shortcut)

class ShowAppListEvent()

class DragAppStartEvent

class DragAppEndEvent