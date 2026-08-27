package com.axiel7.lucifer.ui.base.state

import com.axiel7.lucifer.data.model.media.BaseMediaNode
import com.axiel7.lucifer.data.model.media.BaseMyListStatus

interface MediaForEdit {
    val mediaInfo: BaseMediaNode?
    val myListStatus: BaseMyListStatus?
}