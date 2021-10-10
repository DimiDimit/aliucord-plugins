package tk.zt64.plugins

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.discord.databinding.WidgetHomeBinding
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.home.WidgetHomeHeaderManager
import com.discord.widgets.home.WidgetHomeModel
import tk.zt64.plugins.noburnin.PluginSettings

@AliucordPlugin
class NoBurnIn : Plugin() {
    private val logger = Logger("NoBurnIn")

    init {
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    override fun start(context: Context) {
        val toolbarIconId = Utils.getResId("toolbar_icon", "id")
        val searchButtonId = Utils.getResId("menu_chat_search", "id")
        val threadButtonId = Utils.getResId("menu_chat_thread_browser", "id")
        val membersButtonId = Utils.getResId("menu_chat_side_panel", "id")
        val callButtonId = Utils.getResId("menu_chat_start_call", "id")
        val videoButtonId = Utils.getResId("menu_chat_start_video_call", "id")

        if (settings.getBool("immersiveMode", false)) {
            WindowInsetsControllerCompat(Utils.appActivity.window, Utils.appActivity.findViewById<ViewGroup>(android.R.id.content)).hide(
                when (settings.getInt("immersiveModeType", 0)) {
                    0 -> WindowInsetsCompat.Type.statusBars()
                    1 -> WindowInsetsCompat.Type.navigationBars()
                    2 -> WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.statusBars()
                    else -> return
                }
            )
        }

        patcher.patch(WidgetHomeHeaderManager::class.java.getDeclaredMethod("configure", WidgetHome::class.java, WidgetHomeModel::class.java, WidgetHomeBinding::class.java), Hook {
            with(it.args[0] as WidgetHome) {
                val menu = toolbar.menu

                if (settings.getBool("hideToolbar", false)) {
                    toolbar.visibility = View.GONE
                    unreadCountView.visibility = View.GONE
                } else {
                    if (settings.getBool("hideChannelIcon", false)) {
                        val root = actionBarTitleLayout?.i?.root

                        if (root == null)
                            logger.warn("Unable to get binding for toolbar title, so the icon will not be hidden. Please let the plugin developer know")
                        else
                            root.findViewById<ImageView>(toolbarIconId)?.visibility = View.GONE
                    }

                    if (settings.getBool("hideText", false)) {
                        setActionBarTitle("")
                        setActionBarSubtitle("")
                    }

                    if (settings.getBool("hideUnread", true)) unreadCountView.visibility = View.GONE
                    if (settings.getBool("hideDrawerButton", true)) setActionBarDisplayHomeAsUpEnabled(false)
                    if (settings.getBool("hideSearchButton", true)) menu.findItem(searchButtonId)?.isVisible = false
                    if (settings.getBool("hideThreadsButton", true)) menu.findItem(threadButtonId)?.isVisible = false
                    if (settings.getBool("hideMembersButton", true)) menu.findItem(membersButtonId)?.isVisible = false
                    if (settings.getBool("hideCallButton", true)) menu.findItem(callButtonId)?.isVisible = false
                    if (settings.getBool("hideVideoButton", true)) menu.findItem(videoButtonId)?.isVisible = false
                }
            }
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}