package ru.ryakovlev.spiritlauncher.base.view

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import com.hannesdorfmann.mosby3.mvp.MvpFragment
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import android.os.VibrationEffect
import android.os.Build
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import ru.ryakovlev.spiritlauncher.R


/**
 * Created by roma on 01.11.2017.
 */
abstract class BaseFragment<V : MvpView, P : MvpPresenter<V>> : MvpFragment<V, P>() {

    fun getWindowHeight() : Int{
        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size.y
    }

    fun getWindowWidth() : Int{
        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size.x
    }

    fun longClickConfirm(){
        val v = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, 10))
        } else {
            v.vibrate(30)
        }
    }

    fun getShortcutAnimation(isOnTop: Boolean) : LayoutAnimationController {
        val resId = if(isOnTop){
            R.anim.layout_animation_slide
        }else{
            R.anim.layout_animation_slide
        }
        return AnimationUtils.loadLayoutAnimation(context, resId)
    }
}