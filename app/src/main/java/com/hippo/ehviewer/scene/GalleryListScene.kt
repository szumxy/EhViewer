/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.scene

import android.os.Bundle
import android.view.MenuItem
import com.hippo.ehviewer.EHV_PREFERENCES
import com.hippo.ehviewer.LIST_MODE_BRIEF
import com.hippo.ehviewer.LIST_MODE_DETAIL
import com.hippo.ehviewer.R
import com.hippo.ehviewer.activity.EhvActivity
import com.hippo.ehviewer.client.GLUrlBuilder
import com.hippo.ehviewer.client.data.GalleryInfo
import com.hippo.ehviewer.mvp.EhvScene
import com.hippo.ehviewer.mvp.MvpPaper
import com.hippo.ehviewer.mvp.MvpPen
import com.hippo.ehviewer.slice.DumpPen
import com.hippo.ehviewer.slice.GalleryListPen
import com.hippo.ehviewer.slice.StatusBarPaper
import com.hippo.ehviewer.slice.StatusBarPen
import com.hippo.ehviewer.slice.ToolbarPaper
import com.hippo.ehviewer.slice.ToolbarPen
import com.hippo.ehviewer.slice.galleryList
import com.hippo.ehviewer.slice.papers
import com.hippo.ehviewer.slice.pens
import com.hippo.ehviewer.slice.setTitle
import com.hippo.ehviewer.slice.statusBar
import com.hippo.ehviewer.slice.toolbar

/*
 * Created by Hippo on 2017/7/24.
 */

/**
 * Shows gallery list. Only this gallery list, can't switch to another list.
 */
class GalleryListScene : EhvScene() {

  companion object {
    val KEY_GL_URL_BUILDER = "GalleryListScene:gl_url_builder"
  }

  private val statusBar: StatusBarPen = object : StatusBarPen() {}

  private val toolbar: ToolbarPen = object : ToolbarPen() {
    override fun onDoubleClick() {
      galleryList.refresh()
    }

    override fun onClickNavigationIcon() {
      pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean  = when (item.itemId) {
      R.id.gallery_list_action_go_to -> {
        // TODO
        true
      }
      R.id.gallery_list_action_detail -> {
        EHV_PREFERENCES.listMode.value = LIST_MODE_DETAIL
        true
      }
      R.id.gallery_list_action_brief -> {
        EHV_PREFERENCES.listMode.value = LIST_MODE_BRIEF
        true
      }
      else -> super.onClickMenuItem(item)
    }
  }

  private val galleryList: GalleryListPen = object : GalleryListPen() {
    override fun showMessage(message: String) {
      (activity as? EhvActivity)?.snack(message)
    }

    override fun onClickGalleryInfo(info: GalleryInfo) {
      stage?.pushScene(galleryDetail(info))
    }
  }

  private lateinit var pen: DumpPen

  override fun createPen(args: Bundle): MvpPen<*> = pens(statusBar, toolbar, galleryList) {
    toolbar.setDoubleClickEnabled()
    toolbar.setNavigationIcon(R.drawable.arrow_left_dark_x24)

    // Register menu updater
    EHV_PREFERENCES.listMode.observable.register {
      when (it) {
      // If inflating menu at once, the text in dialog will change at once, that looks bad
        LIST_MODE_DETAIL -> schedule({ toolbar.inflateMenu(R.menu.gallery_list_detail) }, 200)
        LIST_MODE_BRIEF -> schedule({ toolbar.inflateMenu(R.menu.gallery_list_brief) }, 200)
      }
    }

    // Register title updater
    galleryList.builderObservable.register { toolbar.setTitle(it.toTitle()) }

    val builder = args.getParcelable<GLUrlBuilder>(KEY_GL_URL_BUILDER)
    galleryList.apply(builder)

  }.apply { pen = this }

  override fun createPaper(): MvpPaper<*> = papers(pen) {
    statusBar(statusBar, it) {
      toolbar(toolbar, StatusBarPaper.CONTAINER_ID) {
        galleryList(galleryList, ToolbarPaper.CONTAINER_ID)
      }
    }
  }
}