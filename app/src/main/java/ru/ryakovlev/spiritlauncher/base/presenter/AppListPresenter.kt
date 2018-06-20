package ru.ryakovlev.spiritlauncher.base.presenter

import android.content.Context
import android.content.Intent
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
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.coroutines.experimental.bg
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import ru.ryakovlev.spiritlauncher.event.DragAppEndEvent
import ru.ryakovlev.spiritlauncher.event.DragAppStartEvent
import ru.ryakovlev.spiritlauncher.event.ShortcutEvent
import ru.ryakovlev.spiritlauncher.event.StartApplicationEvent


/**
 * Created by roma on 28.05.2018.
 */
class AppListPresenter<V : AppListPresenter.View> : MvpBasePresenter<V>() {

    private var appList: List<ApplicationInfo>? = null
    private var shownList: List<ApplicationInfo>? = null

    fun load(context: Context) {
        val pm = context.packageManager

        async(UI) {
            val data: Deferred<List<ApplicationInfo>> = bg {
                val i = Intent(Intent.ACTION_MAIN, null)
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(i, 0)
                        .map { ApplicationInfo(label = it.loadLabel(pm), packageName = it.activityInfo.packageName, icon = it.activityInfo.loadIcon(pm)) }.sortedBy { it.label.toString() }
            }
            appList = data.await()
            shownList = appList
            view.showApplications(appList!!)
        }
    }

    fun appLongTap(context: Context, position: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val shortcutQuery = LauncherApps.ShortcutQuery()

            shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            shortcutQuery.setPackage(shownList!![position].packageName.toString())
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

    fun appClicked(position: Int) {
        EventBus.getDefault().post(StartApplicationEvent(shownList!![position].packageName.toString()))
    }

    fun shortcutClicked(item: Shortcut) {
        EventBus.getDefault().post(ShortcutEvent(item))
    }

    fun filterTextChanged(filterText: String) {
        appList?.let {
            async(UI) {
                val data: Deferred<List<ApplicationInfo>> = bg {
                    filterAppList(it, filterText)
                }
                shownList = data.await()
                view.showApplications(shownList!!)
            }
        }
    }

    private fun filterAppList(appList: List<ApplicationInfo>, filterText: String) = appList.filter { app ->
        filterText.isEmpty() || filterText.toLowerCase().split(" ").all {
            subFilter -> app.label.toString().toLowerCase().split(" ").any { subLabel -> subLabel.startsWith(subFilter) }
        }
    }

    interface View : MvpView {
        fun showApplications(applications: List<ApplicationInfo>)

        fun showShortcuts(shortcutList: List<Shortcut>, position: Int)

        fun dragApp(position: Int)
    }
}