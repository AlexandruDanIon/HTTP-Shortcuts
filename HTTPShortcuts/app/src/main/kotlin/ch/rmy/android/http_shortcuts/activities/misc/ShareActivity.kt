package ch.rmy.android.http_shortcuts.activities.misc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver

class ShareActivity : BaseActivity() {

    private val controller by lazy {
        destroyer.own(Controller())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isRealmAvailable) {
            return
        }

        val type = intent.type
        val action = intent.action
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (type == TYPE_TEXT && action == Intent.ACTION_SEND && text != null) {
            handleTextSharing(text)
        } else {
            handleFileSharing(getFileUris() ?: return)
        }
    }

    private fun getFileUris(): List<Uri>? =
        if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let {
                listOf(it)
            }
        } else {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }

    private fun handleTextSharing(text: String) {
        val variableLookup = VariableManager(controller.getVariables())
        val variables = getTargetableVariablesForTextSharing()
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTargetableShortcutsForTextSharing(variableIds, variableLookup)

        val variableValues = variables.associate { variable -> variable.key to text }
        when (shortcuts.size) {
            0 -> showInstructions(R.string.error_not_suitable_shortcuts)
            1 -> {
                executeShortcut(shortcuts[0], variableValues = variableValues)
                finishWithoutAnimation()
            }
            else -> showShortcutSelection(shortcuts, variableValues = variableValues)
        }
    }

    private fun getTargetableVariablesForTextSharing() =
        controller
            .getVariables()
            .filter { it.isShareText }
            .toSet()

    private fun getTargetableShortcutsForTextSharing(variableIds: Set<String>, variableLookup: VariableLookup): List<Shortcut> =
        controller
            .getShortcuts()
            .filter { hasShareVariable(it, variableIds, variableLookup) }

    private fun getTargetableShortcutsForFileSharing(): List<Shortcut> =
        controller
            .getShortcuts()
            .filter { hasFileParameter(it) }

    private fun handleFileSharing(fileUris: List<Uri>) {
        val shortcuts = getTargetableShortcutsForFileSharing()
        when (shortcuts.size) {
            0 -> showInstructions(R.string.error_not_suitable_shortcuts)
            1 -> {
                executeShortcut(shortcuts[0], files = fileUris)
                finishWithoutAnimation()
            }
            else -> showShortcutSelection(shortcuts, files = fileUris)
        }
    }

    private fun executeShortcut(shortcut: Shortcut, variableValues: Map<String, String> = emptyMap(), files: List<Uri> = emptyList()) {
        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .variableValues(variableValues)
            .files(files)
            .build()
            .startActivity(this)
    }

    private fun showInstructions(@StringRes text: Int) {
        DialogBuilder(context)
            .message(text)
            .dismissListener { finishWithoutAnimation() }
            .positive(R.string.dialog_ok)
            .showIfPossible()
    }

    private fun showShortcutSelection(shortcuts: List<Shortcut>, variableValues: Map<String, String> = emptyMap(), files: List<Uri> = emptyList()) {
        DialogBuilder(context)
            .mapFor(shortcuts) { builder, shortcut ->
                builder.item(shortcut.name) {
                    executeShortcut(shortcut, variableValues, files)
                }
            }
            .dismissListener { finishWithoutAnimation() }
            .showIfPossible()
    }

    companion object {

        private const val TYPE_TEXT = "text/plain"

        private fun hasShareVariable(shortcut: Shortcut, variableIds: Set<String>, variableLookup: VariableLookup): Boolean {
            val variableIdsInShortcut = VariableResolver.extractVariableIds(shortcut, variableLookup)
            return variableIds.any { variableIdsInShortcut.contains(it) }
        }

        private fun hasFileParameter(shortcut: Shortcut): Boolean =
            shortcut.parameters.any { it.isFileParameter || it.isFilesParameter }

    }

}
