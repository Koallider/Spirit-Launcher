package ru.ryakovlev.spiritlauncher.base.view.applist

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.app_list_fragment.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.adapter.ApplicationInfoAdapter
import ru.ryakovlev.spiritlauncher.base.presenter.AppListPresenter
import ru.ryakovlev.spiritlauncher.base.view.BaseFragment
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo

/**
 * Created by roma on 28.05.2018.
 */
class AppListView : AppListPresenter.View, BaseFragment<AppListView, AppListPresenter<AppListView>>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.app_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.load(context!!)

        appList.layoutManager = GridLayoutManager(activity, 4)
        appList.isNestedScrollingEnabled = false

        searchBarPanel.cardElevation = 0f
        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            if (searchBarPanel != null && scrollView.canScrollVertically(-1)) {
                searchBarPanel.cardElevation = 14f
            } else {
                searchBarPanel.cardElevation = 0f
            }
        })

        searchBar.textChangedListener {
            onTextChanged { text, start, before, count ->
                presenter.filterTextChanged(text.toString())
            }
        }
    }

    override fun createPresenter() = AppListPresenter<AppListView>()

    override fun showApplications(applications: List<ApplicationInfo>) {
        appList.adapter = ApplicationInfoAdapter(applications) {
            presenter.appClicked(it)
        }
    }
}