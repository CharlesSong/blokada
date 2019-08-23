package gs.environment

import android.content.Context
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware

/**
 * Contains various structures related to Android Context.
 */

val Context.inject: () -> Kodein get() = { (applicationContext as KodeinAware).kodein }

