package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class ToastAction(
    actionType: ToastActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val message: String = data[KEY_TEXT] ?: ""

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableManager.getVariableValuesByIds())
        return if (finalMessage.isNotEmpty()) {
            Completable
                .fromAction {
                    context.showToast(finalMessage, long = true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }

    companion object {

        const val KEY_TEXT = "text"

    }

}