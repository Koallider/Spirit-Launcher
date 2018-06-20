package ru.ryakovlev.spiritlauncher.base.presenter

import android.arch.lifecycle.LiveData
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
import org.jetbrains.anko.coroutines.experimental.bg
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.HomeScreenIcon
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import ru.ryakovlev.spiritlauncher.event.ShortcutEvent
import ru.ryakovlev.spiritlauncher.event.ShowAppListEvent
import ru.ryakovlev.spiritlauncher.event.StartApplicationEvent


/**
 * Created by roma on 28.05.2018.
 */
class HomePagePresenter<V : HomePagePresenter.View> : MvpBasePresenter<V>() {

    fun load(context: Context) {
        /*val pm = context.packageManager

        async(UI) {
            val data: Deferred<List<ApplicationInfo>> = bg {
                val i = Intent(Intent.ACTION_MAIN, null)
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(i, 0)
                        .map { ApplicationInfo(label = it.loadLabel(pm), packageName = it.activityInfo.packageName, icon = it.activityInfo.loadIcon(pm)) }.sortedBy { it.label.toString() }
            }
            appList = data.await()
            view.showApplications(appList!!)
        }*/
    }

    fun appListCLicked(){
        EventBus.getDefault().post(ShowAppListEvent())
    }

    interface View : MvpView {
        fun showApplications(applications: List<ApplicationInfo>)

    }
}