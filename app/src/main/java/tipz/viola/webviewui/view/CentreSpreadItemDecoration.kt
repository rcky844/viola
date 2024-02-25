/*
 * Copyright (c) 2023-2024 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.webviewui.view

import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class CentreSpreadItemDecoration(private val itemWidth: Float, private val itemCount: Int, private val isLinear: Boolean) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val useFixedMgr = parent.layoutManager is FixedLinearLayoutManager

        val displayMetrics: DisplayMetrics = parent.resources.displayMetrics
        val totalWidth = displayMetrics.widthPixels

        val totalWidthFromItems = itemWidth * itemCount
        if (totalWidthFromItems >= totalWidth) return

        val totalRequiredSpace = totalWidth - totalWidthFromItems
        if (totalRequiredSpace < itemCount) return

        val requiredSpace = totalRequiredSpace / (itemCount * 2) - 1
        outRect.left = if (parent.getChildLayoutPosition(view) == 0 && isLinear) (requiredSpace / 2).toInt() else requiredSpace.toInt()
        outRect.right = if (parent.getChildLayoutPosition(view) == itemCount && isLinear) (requiredSpace / 2).toInt() else requiredSpace.toInt()

        if (useFixedMgr) (parent.layoutManager as FixedLinearLayoutManager).setScrollEnabled(false)
    }
}