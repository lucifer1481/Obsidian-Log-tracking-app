package com.axiel7.lucifer.data.model.media

import com.axiel7.lucifer.data.model.anime.Ranking

interface BaseRanking {
    val node: BaseMediaNode
    val ranking: Ranking?
    val rankingType: RankingType?
}