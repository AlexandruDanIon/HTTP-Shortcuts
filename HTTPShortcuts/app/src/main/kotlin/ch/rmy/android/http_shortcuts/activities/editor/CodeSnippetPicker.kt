package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Context
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

class CodeSnippetPicker(private val context: Context, private val variablePlaceholderProvider: VariablePlaceholderProvider) {

    fun showCodeSnippetPicker(insertText: (before: String, after: String) -> Unit, includeResponseOptions: Boolean = true, includeNetworkErrorOption: Boolean = false) {
        MenuDialogBuilder(context)
            .title(R.string.title_add_code_snippet)
            .mapIf(includeResponseOptions) {
                it.item(R.string.dialog_code_snippet_handle_response) {
                    showResponseOptionsPicker(insertText, includeNetworkErrorOption)
                }
            }
            .item(R.string.dialog_code_snippet_variables) {
                showVariablesOptionsPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_actions) {
                showActionsPicker(insertText)
            }
            .showIfPossible()
    }

    private fun showResponseOptionsPicker(insertText: (before: String, after: String) -> Unit, includeNetworkErrorOption: Boolean = false) {
        MenuDialogBuilder(context)
            .item(R.string.dialog_code_snippet_response_body) {
                insertText("response.body", "")
            }
            .item(R.string.dialog_code_snippet_response_headers) {
                insertText("response.headers", "")
            }
            .item(R.string.dialog_code_snippet_response_status_code) {
                insertText("response.statusCode", "")
            }
            .item(R.string.dialog_code_snippet_response_cookies) {
                insertText("response.cookies", "")
            }
            .mapIf(includeNetworkErrorOption) {
                it.item(R.string.dialog_code_snippet_response_network_error) {
                    insertText("networkError", "")
                }
            }
            .showIfPossible()
    }

    private fun showVariablesOptionsPicker(insertText: (before: String, after: String) -> Unit) {
        MenuDialogBuilder(context)
            .item(R.string.dialog_code_snippet_get_variable) {
                MenuDialogBuilder(context)
                    .mapFor(variablePlaceholderProvider.placeholders) { builder, variable ->
                        builder.item(variable.variableKey) {
                            insertText("getVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/)", "")
                        }
                    }
                    .showIfPossible()
            }
            .item(R.string.dialog_code_snippet_set_variable) {
                MenuDialogBuilder(context)
                    .mapFor(variablePlaceholderProvider.constantsPlaceholders) { builder, variable ->
                        builder.item(variable.variableKey) {
                            insertText("setVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/, \"", "\");")
                        }
                    }
                    .showIfPossible()
            }
            .showIfPossible()
    }

    private fun showActionsPicker(insertText: (before: String, after: String) -> Unit) {
        MenuDialogBuilder(context)
            .item(R.string.action_type_toast_title) {
                insertText("showToast(\"", "\");")
            }
            .item(R.string.action_type_dialog_title) {
                insertText("showDialog(\"Message\"", ", \"Title\");")
            }
            .mapIf((context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
                it.item(R.string.action_type_vibrate_title) {
                    insertText("vibrate();", "")
                }
            }
            .item(R.string.action_type_trigger_shortcut_title) {
                insertText("triggerShortcut(\"shortcut name or ID\", \"\");", "")
            }
            .mapIf(LauncherShortcutManager.supportsPinning(context)) {
                it.item(R.string.action_type_rename_shortcut_title) {
                    insertText("renameShortcut(\"shortcut name or ID\", \"\", \"new name\");", "")
                }
            }
            .showIfPossible()
    }

}