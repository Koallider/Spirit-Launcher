package ru.ryakovlev.spiritlauncher.util

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo

/**
 * Created by roma on 15.06.2018.
 */
class TouchListener<T>(val item: T): View.OnTouchListener {

    private val LONG_TAP_TIME = 500L

    private var downTime = 0L

    private var downX = 0f
    private var downY = 0f

    private var longTapped = false
    private var moved = false
    private var down = false

    var clickListener: ((item: T) -> Unit)? = null
    var longClickListener: ((item: T) -> Unit)? = null
    var moveListener: ((item: T) -> Unit)? = null

    override fun onTouch(p0: View, event: MotionEvent): Boolean {
        Log.w("touchListener", event.action.toString())

        if(longTapped){
            p0.parent.requestDisallowInterceptTouchEvent(true);
        }

        if (event.action == MotionEvent.ACTION_DOWN)
        {
            down = true
            downTime = System.currentTimeMillis()
            downX = event.x
            downY = event.y


            val handler = Handler()
            handler.postDelayed({
                Log.w("touchListener", "delayed $down $moved")

                if(down && !moved && System.currentTimeMillis() - downTime > LONG_TAP_TIME){
                    longClickListener?.invoke(item)
                    Log.w("touchListener", "longClick")

                    longTapped = true
                }
            }, LONG_TAP_TIME)
        }
        if (event.action == MotionEvent.ACTION_MOVE)
        {
            if(down && Math.abs(event.x - downX) > 10 || Math.abs(event.y - downY) > 10) {
                if(longTapped) {
                    Log.w("touchListener", "drag")
                    down = false
                    moveListener?.invoke(item)

                }else{
                    Log.w("touchListener", "scroll")

                    moved = true
                }
            }
        }
        if (event.action == MotionEvent.ACTION_UP)
        {
            if(System.currentTimeMillis() - downTime < LONG_TAP_TIME){
                Log.w("touchListener", "click")

                clickListener?.invoke(item);
            }
            reset()
        }
        if (event.action == MotionEvent.ACTION_CANCEL)
        {
            reset()
        }
        return true
    }

    private fun reset(){
        down = false
        longTapped = false
        moved = false
        downTime = 0
        downX = 0f
        downY = 0f
    }
}