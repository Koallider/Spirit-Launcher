package ru.ryakovlev.spiritlauncher.base.view.applist

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.app_list_fragment.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.wrapContent
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.adapter.ApplicationInfoAdapter
import ru.ryakovlev.spiritlauncher.adapter.ShortcutAdapter
import ru.ryakovlev.spiritlauncher.base.presenter.AppListPresenter
import ru.ryakovlev.spiritlauncher.base.view.BaseFragment
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.view.View.GONE


/**
 * Created by roma on 28.05.2018.
 */
class AppListView : AppListPresenter.View, BaseFragment<AppListView, AppListPresenter<AppListView>>() {

    lateinit var adapter: ApplicationInfoAdapter

    var shortcutPopup: PopupWindow? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.app_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appList.layoutManager = GridLayoutManager(activity, 4)
        appList.isNestedScrollingEnabled = false

        searchBarPanel.cardElevation = 0f
        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            shortcutPopup?.dismiss()
            if (searchBarPanel != null && scrollView.canScrollVertically(-1)) {
                searchBarPanel.cardElevation = 14f
            } else {
                searchBarPanel.cardElevation = 0f
            }
        })

        searchBar.textChangedListener {
            onTextChanged { text, _, _, _ ->
                presenter.filterTextChanged(text.toString())
            }
        }
        adapter = ApplicationInfoAdapter(ArrayList<ApplicationInfo>())

        adapter.clickListener = {presenter.appClicked(it)}
        adapter.longClickListener = {position->
            longClickConfirm()
            presenter.appLongTap(context!!, position)
        }
        adapter.moveListener = {position->
            presenter.appMoved(position)
            longClickConfirm()
        }

        appList.adapter = adapter
        presenter.load(context!!)
    }

    override fun createPresenter() = AppListPresenter<AppListView>()

    override fun showApplications(applications: List<ApplicationInfo>) {
        adapter.applications.clear()
        adapter.applications.addAll(applications)
        adapter.notifyDataSetChanged()
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    override fun showShortcuts(shortcutList: List<Shortcut>, position: Int) {
        shortcutPopup?.dismiss()
        if(shortcutList.isNotEmpty()){
            val anchor = appList.findViewHolderForAdapterPosition(position).itemView;
            val location = IntArray(2, {0})
            anchor.getLocationOnScreen(location)

            val shortcutRecyclerView = RecyclerView(context)
            shortcutRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            shortcutRecyclerView.adapter = ShortcutAdapter(shortcutList = shortcutList, clickListener = {
                shortcut: Shortcut -> presenter.shortcutClicked(shortcut)
                shortcutPopup?.dismiss()
            })
            val launcherApps = activity?.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            shortcutList.forEach{it.drawable = launcherApps.getShortcutIconDrawable(it.shortcutInfo,  resources.displayMetrics.densityDpi)}

            val height = getWindowHeight()
            shortcutPopup = PopupWindow(shortcutRecyclerView, wrapContent, wrapContent, true)
            val recyclerHeight = shortcutList.size * dip(54)//TODO hardcoded shortcut item height
            if(height < location[1] + anchor.height + recyclerHeight) {
                shortcutPopup!!.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.width / 2, location[1] - recyclerHeight)
                shortcutRecyclerView.layoutAnimation = getShortcutAnimation(true)
            }else{
                shortcutPopup!!.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.width / 2, location[1] + anchor.height - dip(20))
                shortcutRecyclerView.layoutAnimation = getShortcutAnimation(false)
            }
        }
    }

    override fun dragApp(position: Int){
        shortcutPopup?.dismiss()

        val anchor = appList.findViewHolderForAdapterPosition(position).itemView;

        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(
                anchor)
        anchor.startDrag(data, shadowBuilder, anchor, 0)
        //anchor.visibility = View.INVISIBLE
        root.visibility = GONE
    }
}