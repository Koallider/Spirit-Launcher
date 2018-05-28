package ru.ryakovlev.spiritlauncher.base.view

import android.os.Bundle
import com.hannesdorfmann.mosby3.mvp.MvpFragment
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * Created by roma on 01.11.2017.
 */
abstract class BaseFragment<V : MvpView, P : MvpPresenter<V>> : MvpFragment<V, P>()