package com.ljwx.basemediaplayer

import android.content.Context
import android.util.AttributeSet
import androidx.media3.ui.PlayerView

class LjwxPlayerView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    theme: Int = 0
) : PlayerView(context, attr, theme) {

    fun setLjwxPlayer(player: LjwxMediaPlayer) {
        super.setPlayer(player.getPlayer())
    }

}