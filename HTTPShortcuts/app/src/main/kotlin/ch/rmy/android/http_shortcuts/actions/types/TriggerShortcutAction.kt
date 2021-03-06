package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class TriggerShortcutAction(
    actionType: TriggerShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val shortcutNameOrId: String = data[KEY_SHORTCUT_NAME_OR_ID] ?: ""

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return Completable
                .fromAction {
                    context.showToast(R.string.action_type_trigger_shortcut_error_recursion_depth_reached, long = true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
        }
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId)
            ?: return Completable
                .fromAction {
                    context.showToast(String.format(context.getString(R.string.error_shortcut_not_found_for_triggering), shortcutNameOrId), long = true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
        return Commons.createPendingExecution(
            shortcutId = shortcut.id,
            tryNumber = 0,
            waitUntil = DateUtil.calculateDate(shortcut.delay),
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = recursionDepth + 1
        )
    }

    companion object {

        const val KEY_SHORTCUT_NAME_OR_ID = "shortcutId"

        private const val MAX_RECURSION_DEPTH = 5

    }

}