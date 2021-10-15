package com.aliucord.plugins

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.utils.DimenUtils
import com.discord.api.premium.PremiumTier
import com.discord.databinding.WidgetChatOverlayBinding
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.input.AppFlexInputViewModel
import com.discord.widgets.chat.overlay.`WidgetChatOverlay$binding$2`
import com.lytefast.flexinput.R

@AliucordPlugin
class CharCounter : Plugin() {
    override fun start(context: Context) {
        var counter: TextView? = null

        patcher.patch(`WidgetChatOverlay$binding$2`::class.java.getDeclaredMethod("invoke", View::class.java), Hook {
            val root = (it.result as WidgetChatOverlayBinding).root as ConstraintLayout

            counter = TextView(root.context, null, 0, R.h.UiKit_TextView).apply {
                id = View.generateViewId()
                visibility = View.GONE
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, DimenUtils.dpToPx(24)).apply {
                    rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                setTextSize(TypedValue.COMPLEX_UNIT_PX, root.resources.getDimension(Utils.getResId("uikit_textsize_small", "dimen")))
                setBackgroundColor(ColorCompat.getThemedColor(root.context, R.b.primary_630))
                root.addView(this)
            }

            (root.findViewById<RelativeLayout>(Utils.getResId("chat_overlay_typing", "id"))
                .layoutParams as ConstraintLayout.LayoutParams).apply {
                    rightToLeft = counter!!.id
                    width = WRAP_CONTENT
                }
        })

        patcher.patch(AppFlexInputViewModel::class.java.getDeclaredMethod("onInputTextChanged", String::class.java, Boolean::class.javaObjectType), Hook {
            val str = it.args[0] as String
            val maxChars = if (StoreStream.getUsers().me.premiumTier == PremiumTier.TIER_2) "4000" else "2000"

            counter?.apply {
                visibility = if (str == "") View.GONE else View.VISIBLE
                text = String.format("%s/%s", str.length, maxChars)
            }
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}