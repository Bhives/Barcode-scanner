package com.reproflex3.rplink.utils

import android.view.View
import androidx.core.view.isVisible

inline fun View.setIsVisibleBy(visibilityCondition: () -> Boolean) {
    isVisible = visibilityCondition.invoke()
}