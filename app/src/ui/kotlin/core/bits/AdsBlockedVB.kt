package core.bits

import com.github.salomonbrys.kodein.instance
import core.*
import core.bits.menu.MENU_CLICK_BY_NAME
import g11n.i18n
import gs.property.IWhen
import org.blokada.R
import tunnel.BLOCKA_CONFIG
import tunnel.BlockaConfig

class AdsBlockedVB(
        private val ktx: AndroidKontext,
        private val tunnelStatus: EnabledStateActor = ktx.di().instance(),
        private val tunManager: TunnelStateManager = ktx.di().instance()
) : ByteVB() {

    private var droppedCountListener: IWhen? = null
    private var dropped: Int = 0
    private var active = false
    private var activating = false
    private var config: BlockaConfig = BlockaConfig()

    override fun attach(view: ByteView) {
        droppedCountListener = tunnelState.tunnelDropCount.doOnUiWhenSet().then {
            dropped = tunnelState.tunnelDropCount()
            update()
        }
        tunnelStatus.listeners.add(tunnelListener)
        tunnelStatus.update()
        core.on(BLOCKA_CONFIG, configListener)
        update()
    }

    override fun detach(view: ByteView) {
        tunnelState.tunnelDropCount.cancel(droppedCountListener)
        tunnelStatus.listeners.remove(tunnelListener)
        core.cancel(BLOCKA_CONFIG, configListener)
    }

    private val update = {
        view?.run {
            when {
                !tunnelState.enabled() -> {
                    icon(R.drawable.ic_show.res())
                    label(R.string.home_touch_adblocking.res())
                    state(R.string.home_adblocking_disabled.res())
                    switch(false)
                    arrow(null)
                    onTap {
                        tunManager.turnAdblocking(true)
                    }
                    onSwitch {
                        tunManager.turnAdblocking(it)
                    }
                }
                activating || !active -> {
                    icon(R.drawable.ic_show.res())
                    label(R.string.home_activating.res())
                    state(R.string.home_please_wait.res())
                    switch(null)
                    arrow(null)
                    onTap { }
                    onSwitch {  }
                }
                !config.adblocking && config.blockaVpn -> {
                    icon(R.drawable.ic_show.res())
                    label(R.string.home_vpn_only.res())
                    state(R.string.home_adblocking_disabled.res())
                    switch(false)
                    arrow(null)
                    onTap {
                    }
                    onSwitch {
                        tunManager.turnAdblocking(true)
                    }
                }
                !config.adblocking -> {
                    icon(R.drawable.ic_show.res())
                    label(R.string.home_dns_only.res())
                    state(R.string.home_adblocking_disabled.res())
                    switch(false)
                    arrow(null)
                    onTap {
                    }
                    onSwitch {
                        tunManager.turnAdblocking(true)
                    }
                }
                else -> {
                    val droppedString = i18n.getString(R.string.home_requests_blocked, Format.counter(dropped))
                    icon(R.drawable.ic_blocked.res(), color = R.color.switch_on.res())
                    label(droppedString.res())
                    switch(true)
                    arrow(null)
                    state(R.string.home_adblocking_enabled.res())
                    onTap {
//                        core.emit(SWIPE_RIGHT)
                        core.emit(MENU_CLICK_BY_NAME, R.string.panel_section_ads.res())
                    }
                    onSwitch {
                        tunManager.turnAdblocking(it)
                    }
                }
           }
        }
        Unit
    }

    private val configListener = { cfg: BlockaConfig ->
        config = cfg
        update()
        Unit
    }

    private val tunnelListener = object : IEnabledStateActorListener {
        override fun startActivating() {
            activating = true
            active = false
            update()
        }

        override fun finishActivating() {
            activating = false
            active = true
            update()
        }

        override fun startDeactivating() {
            activating = true
            active = false
            update()
        }

        override fun finishDeactivating() {
            activating = false
            active = false
            update()
        }
    }

}
