package ru.ryakovlev.spiritlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.ryakovlev.spiritlauncher.base.view.HomeScreenView
import ru.ryakovlev.spiritlauncher.base.view.applist.AppListView
import ru.ryakovlev.spiritlauncher.event.DragAppEndEvent
import ru.ryakovlev.spiritlauncher.event.ShortcutEvent
import ru.ryakovlev.spiritlauncher.event.ShowAppListEvent
import ru.ryakovlev.spiritlauncher.event.StartApplicationEvent


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, HomeScreenView()).commit()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorTransparent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dragApplicationEnded(event: DragAppEndEvent){
        supportFragmentManager.popBackStack()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showApplicationList(event: ShowAppListEvent){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack("appList")
        fragmentTransaction.replace(R.id.containerAppList, AppListView()).commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun startApplication(event: StartApplicationEvent){
        val launchIntent = packageManager.getLaunchIntentForPackage(event.packageName)
        startActivity(launchIntent)
    }

    @SuppressLint("NewApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun startApplicationShortcut(event: ShortcutEvent){
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        launcherApps.startShortcut(event.shortcut.shortcutInfo, null, null)
    }
}
