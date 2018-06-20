package ru.ryakovlev.spiritlauncher.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by roma on 05.06.2018.
 */
class DropPanel : FrameLayout {

    var c =0;
    var r =0;

    constructor(context: Context, c: Int, r: Int) : this(context, null) {
        this.c = c
        this.r = r
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

}