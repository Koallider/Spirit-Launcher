package ru.ryakovlev.spiritlauncher.base.presenter

import android.content.Context
import android.content.Intent
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.coroutines.experimental.bg
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.event.StartApplicationEvent


/**
 * Created by roma on 28.05.2018.
 */
class AppListPresenter<V : AppListPresenter.View> : MvpBasePresenter<V>() {

    private var appList: List<ApplicationInfo>? = null

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
            view.showApplications(appList!!)
        }
    }

    fun appClicked(item: ApplicationInfo) {
        EventBus.getDefault().post(StartApplicationEvent(item))
    }

    fun filterTextChanged(filterText: String) {
        appList?.let {
            async(UI) {
                val data: Deferred<List<ApplicationInfo>> = bg {
                    filterAppList(it, filterText)
                }
                view.showApplications(data.await())
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
    }
}