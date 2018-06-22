package ru.ryakovlev.spiritlauncher.base.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.ClipData
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.widget.*
import android.support.v7.widget.GridLayout
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.android.synthetic.main.app_list_fragment.*
import kotlinx.android.synthetic.main.folder_item.*
import kotlinx.android.synthetic.main.home_screen_fragment.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.dip
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.adapter.ApplicationInfoAdapter
import ru.ryakovlev.spiritlauncher.adapter.ShortcutAdapter
import ru.ryakovlev.spiritlauncher.base.presenter.HomeScreenPresenter
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.domain.HomeScreenIcon
import ru.ryakovlev.spiritlauncher.domain.Shortcut
import ru.ryakovlev.spiritlauncher.util.TouchListener
import ru.ryakovlev.spiritlauncher.util.checkItemsAre
import ru.ryakovlev.spiritlauncher.widgets.DropPanel


/**
 * Created by roma on 28.05.2018.
 */
class HomeScreenView : HomeScreenPresenter.View, BaseFragment<HomeScreenView, HomeScreenPresenter<HomeScreenView>>() {


    lateinit var panels: ArrayList<ArrayList<DropPanel>>

    var folderPopupItem: HomeScreenIcon? = null
    var appListView: RecyclerView? = null
    var shortcutPopup: PopupWindow? = null

    override fun showAppList(appList: List<HomeScreenIcon>) {
        //appList.observe(this, Observer { showList(it) })
        showList(appList)
    }

    private fun showList(list: List<HomeScreenIcon>?) {
        clear()
        list?.let {
            for (item in list) {
                panels[item.x][item.y].addView(
                        if (item.packageNameList.size == 1) {
                            createAppView(item)
                        } else {
                            createFolderView(item)
                        }
                )

            }
        }
    }

    override fun updateItem(item: HomeScreenIcon) {
        launch(UI) {
            panels[item.x][item.y].removeAllViews()
            panels[item.x][item.y].addView(
                    if (item.packageNameList.size == 1) {
                        createAppView(item)
                    } else {
                        createFolderView(item)
                    }
            )
        }
    }

    override fun deleteItem(item: HomeScreenIcon) {
        launch(UI) {
            panels[item.x][item.y].removeAllViews()
        }
    }

    private fun clear() {
        panels.flatMap { it }
                .forEach { it.removeAllViews() }
    }

    private fun createAppView(icon: HomeScreenIcon): View {
        val view = LayoutInflater.from(context).inflate(R.layout.app_list_item, null)
        try {
            val packageManager = activity?.packageManager
            val packageName = icon.packageNameList.first()
            val app = activity?.packageManager?.getApplicationInfo(packageName, 0)

            val iconView = packageManager?.getApplicationIcon(app)
            val name = packageManager?.getApplicationLabel(app)
            val label = view.find<TextView>(R.id.label)
            label.text = name
            label.textColor = Color.WHITE
            label.setShadowLayer(2f, 1f, 1f, Color.BLACK)

            view.find<ImageView>(R.id.icon).setImageDrawable(iconView)

            val touchListener = TouchListener(icon)
            touchListener.clickListener = { presenter.onIconClick(icon) }
            touchListener.longClickListener = {
                longClickConfirm()
                presenter.onIconLongClick(activity!!, icon)
            }
            touchListener.moveListener = {
                longClickConfirm()
                presenter.onIconMove(icon)
            }
            view.setOnTouchListener(touchListener)

            view.setTag(R.id.package_tag, icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return view
    }

    private fun createFolderView(icon: HomeScreenIcon): View {
        val view = LayoutInflater.from(context).inflate(R.layout.folder_item, null)
        try {
            val packageManager = activity?.packageManager
            val iconContainer = view.find<GridLayout>(R.id.iconContainer)

            for ((index, packageName) in icon.packageNameList.filterIndexed { index, _ -> index < 4 }.withIndex()) {
                val app = activity?.packageManager?.getApplicationInfo(packageName, 0)
                val iconView = ImageView(activity)

                iconView.layoutParams = createGridLayoutParams(index % 2, index / 2)
                iconView.setImageDrawable(packageManager?.getApplicationIcon(app))
                iconContainer.addView(iconView)
            }
            view.onLongClick {
                longClickConfirm()
                presenter.onIconLongClick(activity!!, icon)
            }
            view.onClick {
                presenter.onIconClick(icon)
            }
            view.setTag(R.id.package_tag, icon)

            val label = view.find<TextView>(R.id.label)
            label.text = icon.name
            label.textColor = Color.WHITE
            label.setShadowLayer(2f, 1f, 1f, Color.BLACK)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return view
    }

    private fun createGridLayoutParams(x: Int, y: Int): GridLayout.LayoutParams {
        val param = GridLayout.LayoutParams()
        param.height = 0
        param.width = 0
        param.setGravity(Gravity.FILL)
        param.columnSpec = GridLayout.spec(x, 1f)
        param.rowSpec = GridLayout.spec(y, 1f)
        return param
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.home_screen_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.useDefaultMargins = false

        panels = ArrayList()
        appListButton.onClick { presenter.appListCLicked() }
        val dragListener = MyDragListener()
        for (i in 0 until 5) {
            panels.add(ArrayList())
            for (j in 0 until 5) {
                val layout = DropPanel(this.context!!, i, j)
                layout.layoutParams = createGridLayoutParams(i, j)
                layout.setOnDragListener(dragListener)

                pager.addView(layout)
                panels[i].add(layout)
            }
        }

        presenter.load(context!!)
    }

    inner class MyDragListener : View.OnDragListener {
        var i = 0;
        var dropped = false

        override fun onDrag(v: View, event: DragEvent): Boolean {
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dropped = false
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    val container = v as DropPanel
                    container.forEachChild {

                        val hoverView = it.find<CardView>(R.id.hover)
                        hoverView.alpha = 0.0f
                        hoverView.visibility = VISIBLE
                        hoverView.animate()
                                .alpha(1.0f)
                                .setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                hoverView.visibility = VISIBLE
                            }
                        })
                    }
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    val container = v as DropPanel
                    container.forEachChild {
                        val hoverView = it.find<CardView>(R.id.hover)
                        hoverView.animate()
                                .alpha(0.0f)
                                .setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                hoverView.visibility = INVISIBLE
                            }
                        })
                    }
                }
                DragEvent.ACTION_DROP -> {
                    dropped = true
                    // Dropped, reassign View to ViewGroup
                    val view = event.localState as View
                    val container = v as DropPanel
                    val tag = view.getTag(R.id.package_tag)
                    when (tag) {
                        is String -> presenter.dragEnded(activity!!, container.c, container.r, tag)
                        is HomeScreenIcon -> presenter.dragEnded(activity!!, container.c, container.r, tag)
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    if (!dropped) {
                        val view = event.localState as View
                        view.visibility = View.VISIBLE
                    }
                }
                else -> {
                }
            }
            return true
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    override fun showShortcuts(shortcutList: List<Shortcut>, x: Int, y: Int) {
        showShortcuts(shortcutList, panels[x][y])
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    override fun showShortcuts(shortcutList: List<Shortcut>, position: Int) {
        showShortcuts(shortcutList, appListView?.findViewHolderForAdapterPosition(position)?.itemView)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun showShortcuts(shortcutList: List<Shortcut>, anchor: View?) {
        shortcutPopup?.dismiss()
        if (anchor != null && shortcutList.isNotEmpty()) {
            val location = IntArray(2, { 0 })
            anchor.getLocationOnScreen(location)

            val shortcutRecyclerView = RecyclerView(context)
            shortcutRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            shortcutRecyclerView.adapter = ShortcutAdapter(shortcutList = shortcutList, clickListener = { shortcut: Shortcut ->
                presenter.shortcutClicked(shortcut)
                shortcutPopup?.dismiss()
                dismissFolderPopup()
            })
            val launcherApps = activity?.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            shortcutList.forEach { it.drawable = launcherApps.getShortcutIconDrawable(it.shortcutInfo, resources.displayMetrics.densityDpi) }

            val height = getWindowHeight()
            shortcutPopup = PopupWindow(shortcutRecyclerView, wrapContent, wrapContent, true)
            val recyclerHeight = shortcutList.size * dip(54)//TODO hardcoded shortcut item height
            if (height < location[1] + anchor.height + recyclerHeight) {
                shortcutPopup!!.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.width / 2, location[1] - recyclerHeight)
                shortcutRecyclerView.layoutAnimation = getShortcutAnimation(true)
            } else {
                shortcutPopup!!.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.width / 2, location[1] + anchor.height - dip(20))
                shortcutRecyclerView.layoutAnimation = getShortcutAnimation(false)
            }
        }
    }

    override fun dragApp(x: Int, y: Int) {
        shortcutPopup?.dismiss()
        if (!paused) {
            val anchor = panels[x][y].firstChild { true };

            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(
                    anchor)
            anchor.startDrag(data, shadowBuilder, anchor, 0)
            anchor.visibility = View.INVISIBLE
        }
    }

    var paused = false;

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun showPopup(item: HomeScreenIcon) {
        dismissFolderPopup()
        folderPopupItem = item
        val anchor = panels[item.x][item.y]
        val location = IntArray(2, { 0 })
        anchor.getLocationOnScreen(location)

        val view = LayoutInflater.from(activity).inflate(R.layout.folder_popup, null)
        appListView = view.find<RecyclerView>(R.id.appList)
        val packageManager = activity?.packageManager

        val appList = item.packageNameList.map {
            val app = activity?.packageManager?.getApplicationInfo(it, 0)
            ApplicationInfo(packageManager?.getApplicationLabel(app) ?: "", it, packageManager?.getApplicationIcon(app))
        }.toMutableList()
        appListView!!.layoutManager = GridLayoutManager(activity, if (item.packageNameList.size > 4) 4 else item.packageNameList.size)
        appListView!!.isNestedScrollingEnabled = false

        val adapter = ApplicationInfoAdapter(appList)

        adapter.clickListener = {
            dismissFolderPopup()
            presenter.appClicked(adapter.applications[it])
        }
        adapter.longClickListener = { position ->
            longClickConfirm()
            presenter.appLongTap(context!!, adapter.applications[position], position)
        }
        adapter.moveListener = { position ->
            presenter.appMoved(position)
            longClickConfirm()
        }
        appListView!!.adapter = adapter

        view.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        val cardView = view.find<CardView>(R.id.cardView)
        cardView.visibility = INVISIBLE

        folderPanel.addView(view)

        val windowHeight = getWindowHeight()
        val windowWidth = getWindowWidth()

        view.post({
            val folderHeidht = if (cardView.measuredHeight > (view.measuredHeight - 32)) (view.measuredHeight - 32) else cardView.measuredHeight
            val folderWidth = cardView.measuredWidth

            val halfAnchorWidth = anchor.width / 2
            fun getFolderLeftMargin() = if (windowWidth < location[0] + halfAnchorWidth + folderWidth) {
                location[0] + halfAnchorWidth - (location[0] + halfAnchorWidth + folderWidth - windowWidth + dip(16))
            } else {
                location[0] + halfAnchorWidth
            }

            val param = RelativeLayout.LayoutParams(cardView.layoutParams)
            param.topMargin = if (windowHeight < location[1] + anchor.height + folderHeidht) {
                if (location[1] - folderHeidht < 0) 16 else location[1] - folderHeidht
            } else {
                location[1]
            }
            param.leftMargin = getFolderLeftMargin()
            param.height = folderHeidht
            cardView.layoutParams = param
            cardView.visibility = VISIBLE
        })

        val name = view.find<TextView>(R.id.name)
        name.text = if(item.name.isEmpty()) getString(R.string.unnamedFolder) else item.name

        val nameInput = view.find<EditText>(R.id.nameInput)
        name.onClick {
            name.visibility = GONE
            nameInput.visibility = VISIBLE
            nameInput.setText(item.name)
            nameInput.post({
                nameInput.requestFocus();
                nameInput.setSelection(item.name.length)
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
            })
        }

        nameInput.textChangedListener { afterTextChanged { folderPopupItem?.name = nameInput.text.toString() } }

        view.onClick {
            dismissFolderPopup()
        }
    }

    private fun dismissFolderPopup() {
        folderPanel.removeAllViews()
        presenter.updateFolder(activity!!, folderPopupItem)
        folderPopupItem = null
    }

    override fun dragApp(position: Int) {
        shortcutPopup?.dismiss()
        val anchor = appListView?.findViewHolderForAdapterPosition(position)?.itemView;

        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(
                anchor)

        anchor?.startDrag(data, shadowBuilder, anchor, 0)
        folderPopupItem?.let {
            presenter.dragStarted(activity!!, it, (appListView?.adapter as ApplicationInfoAdapter).applications[position].packageName.toString())
        }
        dismissFolderPopup()

    }

    override fun createPresenter() = HomeScreenPresenter<HomeScreenView>()


    private class HomeScreenPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return HomePageView()
        }

        override fun getCount(): Int {
            return 3
        }
    }
}