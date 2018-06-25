package ru.ryakovlev.spiritlauncher.base.presenter

import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import android.widget.Toast
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.coroutines.experimental.bg
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.database.HomeScreenDatabase
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.HomeScreenIcon
import ru.ryakovlev.spiritlauncher.domain.HomeScreenItem
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import ru.ryakovlev.spiritlauncher.event.*


/**
 * Created by roma on 28.05.2018.
 */
class HomeScreenPresenter<V : HomeScreenPresenter.View> : MvpBasePresenter<V>() {

    private var appList: MutableList<HomeScreenIcon>? = null;


    fun load(context: Context) {

        async(UI) {
            val data: Deferred<List<HomeScreenIcon>> = bg {
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().getAll()
            }
            appList = data.await().toMutableList()
            view.showAppList(appList!!)
        }
    }

    fun appListCLicked() {
        EventBus.getDefault().post(ShowAppListEvent())
    }

    fun dragEnded(context: Context, x: Int, y: Int, quickAccess: Boolean, droppedItem: HomeScreenIcon) {
        async(UI) {
            bg {
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().delete(droppedItem)
                appList?.remove(droppedItem)
                view.deleteItem(droppedItem)
                dragEnded(context, x, y, quickAccess, droppedItem.packageNameList, droppedItem)
            }
        }
    }

    fun dragEnded(context: Context, x: Int, y: Int, quickAccess: Boolean, packageName: String) {
        async(UI) {
            bg {
                view.folderPopupItem?.let {
                    val newList = it.packageNameList.toMutableList()
                    newList.removeAt(it.packageNameList.indexOfFirst { it == packageName })
                    it.packageNameList = newList
                    HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().update(it)
                    view.updateItem(it)
                }
                dragEnded(context, x, y, quickAccess, listOf(packageName), null)
            }
        }
    }


    private fun dragEnded(context: Context, x: Int, y: Int, quickAccess: Boolean, packageNameList: List<String>, droppedItem: HomeScreenIcon?) {
        val icon = appList?.firstOrNull { it.x == x && it.y == y }

        icon?.let {
            if(droppedItem != null && icon.id == droppedItem.id){
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().insert(it)
                appList?.add(it)
                it.quickAccess = quickAccess
                view.updateItem(it)
            }else {
                it.packageNameList = it.packageNameList + packageNameList
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().update(it)
                it.quickAccess = quickAccess
                view.updateItem(it)
            }

        } ?: run {
            val newIcon = HomeScreenIcon(0, 0, x, y, packageNameList)
            newIcon.name = droppedItem?.name ?: ""
            newIcon.quickAccess = quickAccess
            newIcon.id = HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().insert(newIcon)
            appList?.add(newIcon)
            view.updateItem(newIcon)
        }
        EventBus.getDefault().post(DragAppEndEvent())
    }

    fun onIconLongClick(context: Context, item: HomeScreenIcon) {
        if (item.packageNameList.size > 1) {
            view.dragApp(item.x, item.y, item.quickAccess)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val shortcutQuery = LauncherApps.ShortcutQuery()

            shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            shortcutQuery.setPackage(item.packageNameList[0].toString())
            try {
                val shortcutList = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle()).map { Shortcut(it) }.sortedBy { it.shortcutInfo.id }
                if (shortcutList.isNotEmpty()) {
                    view.showShortcuts(shortcutList, item.x, item.y, item.quickAccess)
                } else {
                    view.dragApp(item.x, item.y, item.quickAccess)
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, R.string.warning_not_default, Toast.LENGTH_SHORT).show()
                //TODO set as default
            }
        } else {
            view.dragApp(item.x, item.y, item.quickAccess)
        }
    }

    fun onIconMove(item: HomeScreenIcon) {
        view.dragApp(item.x, item.y, item.quickAccess)
    }

    fun onIconClick(item: HomeScreenIcon) {
        if (item.packageNameList.size > 1) {
            view.showPopup(item)
        } else {
            EventBus.getDefault().post(StartApplicationEvent(item.packageNameList.first()))
        }
    }

    fun appLongTap(context: Context, applicationInfo: ApplicationInfo, position: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val shortcutQuery = LauncherApps.ShortcutQuery()

            shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            shortcutQuery.setPackage(applicationInfo.packageName.toString())
            try {
                val shortcutList = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle()).map { Shortcut(it) }.sortedBy { it.shortcutInfo.id }
                if(shortcutList.isNotEmpty()){
                    view.showShortcuts(shortcutList, position)
                }else{
                    dragApp(position)
                }
            } catch (e: SecurityException){
                //Toast.makeText(context, R.string.warning_not_default, Toast.LENGTH_SHORT).show()
                //TODO set as default
                dragApp(position)
            }
        }else{
            dragApp(position)
        }
    }

    fun appMoved(position: Int){
        dragApp(position)
    }

    fun dragApp(position: Int){
        view.dragApp(position)
    }


    fun appClicked(applicationInfo: ApplicationInfo) {
        EventBus.getDefault().post(StartApplicationEvent(applicationInfo.packageName.toString()))
    }


    fun shortcutClicked(item: Shortcut) {
        EventBus.getDefault().post(ShortcutEvent(item))
    }

    fun updateFolder(context: Context, folder: HomeScreenIcon?){
        folder?.let {
            async(UI) {
                bg {
                    HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().update(it)
                    view.updateItem(it)
                }
            }
        }
    }

    interface View : MvpView {
        fun showAppList(appList: List<HomeScreenIcon>)

        fun dragApp(x: Int, y: Int, quickAccess: Boolean)

        fun showPopup(item: HomeScreenIcon)

        fun dragApp(position: Int)

        fun showShortcuts(shortcutList: List<Shortcut>, x: Int, y: Int, quickAccess: Boolean)

        fun showShortcuts(shortcutList: List<Shortcut>, position: Int)

        fun updateItem(item: HomeScreenIcon)

        fun deleteItem(item: HomeScreenIcon)

        var folderPopupItem: HomeScreenIcon?
    }

}