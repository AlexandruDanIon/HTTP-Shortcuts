package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import ch.rmy.android.http_shortcuts.R
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner

var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

fun Fragment.showMessageDialog(@StringRes stringRes: Int) {
    MaterialDialog.Builder(context!!)
            .content(stringRes)
            .positiveText(R.string.dialog_ok)
            .show()
}

fun EditText.focus() {
    requestFocus()
    setSelection(text.length)
}

@Suppress("DEPRECATION")
fun ImageView.clearBackground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background = null
    } else {
        setBackgroundDrawable(null)
    }
}

fun LabelledSpinner.fix() {
    val paddingTop = spinner.context.resources.getDimensionPixelSize(R.dimen.spinner_padding_top)
    label.setPadding(0, paddingTop, 0, 0)
    errorLabel.visibility = View.GONE
}

@ColorInt
@Suppress("DEPRECATION")
fun color(context: Context, @ColorRes colorRes: Int): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes, context.theme)
        } else {
            context.resources.getColor(colorRes)
        }

@Suppress("DEPRECATION")
fun drawable(context: Context, @DrawableRes drawableRes: Int): Drawable? =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(drawableRes, context.theme)
        } else {
            context.resources.getDrawable(drawableRes)
        }