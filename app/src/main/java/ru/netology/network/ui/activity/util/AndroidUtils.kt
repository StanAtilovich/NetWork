package ru.netology.network.ui.activity.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class AndroidUtils {
    companion object {
        fun hideKeyboard(view: View) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}