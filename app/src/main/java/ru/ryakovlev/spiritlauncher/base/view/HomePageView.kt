package ru.ryakovlev.spiritlauncher.base.view

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
import android.widget.TextView
import kotlinx.android.synthetic.main.app_list_fragment.*
import kotlinx.android.synthetic.main.home_page_layout.*
import kotlinx.android.synthetic.main.home_screen_fragment.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.wrapContent
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.adapter.ApplicationInfoAdapter
import ru.ryakovlev.spiritlauncher.adapter.ShortcutAdapter
import ru.ryakovlev.spiritlauncher.base.presenter.AppListPresenter
import ru.ryakovlev.spiritlauncher.base.presenter.HomePagePresenter
import ru.ryakovlev.spiritlauncher.base.presenter.HomeScreenPresenter
import ru.ryakovlev.spiritlauncher.base.view.BaseFragment
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import android.view.DragEvent
import android.widget.LinearLayout
import android.R.attr.shape
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View.OnDragListener




/**
 * Created by roma on 28.05.2018.
 */
class HomePageView : HomePagePresenter.View, BaseFragment<HomePageView, HomePagePresenter<HomePageView>>() {
    override fun showApplications(applications: List<ApplicationInfo>) {
        for (i in 0 until 5){
            for (j in 0 until 5){
                val view = LayoutInflater.from(activity).inflate(R.layout.app_list_item, null)
                val textview = view.find<TextView>(R.id.label)
                textview.text = applications[i * 5 + j].label

                iconGrid.addView(view)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.home_page_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.load(context!!)

    }


    override fun createPresenter() = HomePagePresenter<HomePageView>()
}