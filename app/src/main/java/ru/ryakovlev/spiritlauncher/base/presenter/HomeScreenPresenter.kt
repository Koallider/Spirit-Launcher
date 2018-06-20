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
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import ru.ryakovlev.spiritlauncher.event.*


/**
 * Created by roma on 28.05.2018.
 */
class HomeScreenPresenter<V : HomeScreenPresenter.View> : MvpBasePresenter<V>() {

    private var appList: LiveData<List<HomeScreenIcon>>? = null;


    fun load(context: Context) {

        async(UI) {
            val data: Deferred<LiveData<List<HomeScreenIcon>>> = bg {
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().getAll()
            }
            appList = data.await()
            view.showAppList(appList!!)
        }


    }

    fun appListCLicked() {
        EventBus.getDefault().post(ShowAppListEvent())
    }

    fun dragEnded(context: Context, x: Int, y: Int, droppedItem: HomeScreenIcon) {
        async(UI) {
            bg {
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().delete(droppedItem)

                dragEnded(context, x, y, droppedItem.packageNameList, droppedItem)
            }
        }
    }

    fun dragEnded(context: Context, x: Int, y: Int, packageName: String) {
        async(UI) {
            bg {
                dragEnded(context, x, y, listOf(packageName), null)
            }
        }
    }


    private fun dragEnded(context: Context, x: Int, y: Int, packageNameList: List<String>, droppedItem: HomeScreenIcon?) {
        val icon = appList?.value?.firstOrNull { it.x == x && it.y == y }
        icon?.let {
            if(droppedItem != null && icon.id == droppedItem.id){
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().insert(it)
            }else {
                val newIcon = it.copy(packageNameList = it.packageNameList + packageNameList)
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().deleteAndInsert(it, newIcon)
            }

        } ?: run {
            HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().insert(HomeScreenIcon(x, y, packageNameList))
        }
        EventBus.getDefault().post(DragAppEndEvent())
    }

    fun onIconLongClick(context: Context, item: HomeScreenIcon) {
        if (item.packageNameList.size > 1) {
            view.dragApp(item.x, item.y)
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
                    view.showShortcuts(shortcutList, item.x, item.y)
                } else {
                    view.dragApp(item.x, item.y)
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, R.string.warning_not_default, Toast.LENGTH_SHORT).show()
                //TODO set as default
            }
        } else {
            view.dragApp(item.x, item.y)
        }
    }

    fun onIconMove(item: HomeScreenIcon) {
        view.dragApp(item.x, item.y)
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
                Toast.makeText(context, R.string.warning_not_default, Toast.LENGTH_SHORT).show()
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

    fun dragStarted(context: Context, folderIcon: HomeScreenIcon, packageName: String){
        async(UI) {
            bg {
                val newList = folderIcon.packageNameList.toMutableList()
                newList.removeAt(folderIcon.packageNameList.indexOfFirst { it == packageName })
                val newIcon = folderIcon.copy(packageNameList = newList)
                HomeScreenDatabase.getInstance(context)!!.homeScreenIconDao().deleteAndInsert(folderIcon, newIcon)
            }
        }
    }

    fun appClicked(applicationInfo: ApplicationInfo) {
        EventBus.getDefault().post(StartApplicationEvent(applicationInfo.packageName.toString()))
    }


    fun shortcutClicked(item: Shortcut) {
        EventBus.getDefault().post(ShortcutEvent(item))
    }

    interface View : MvpView {
        fun showAppList(appList: LiveData<List<HomeScreenIcon>>)

        fun dragApp(x: Int, y: Int)

        fun showPopup(item: HomeScreenIcon)

        fun dragApp(position: Int)

        fun showShortcuts(shortcutList: List<Shortcut>, x: Int, y: Int)

        fun showShortcuts(shortcutList: List<Shortcut>, position: Int);
    }

}