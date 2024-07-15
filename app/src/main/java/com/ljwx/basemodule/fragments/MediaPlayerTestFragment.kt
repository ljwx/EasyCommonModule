package com.ljwx.basemodule.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.basefragment.BaseBindingFragment
import com.ljwx.basemediaplayer.LjwxMediaPlayer
import com.ljwx.basemediaplayer.LjwxMediaPlayerListener
import com.ljwx.basemodule.R
import com.ljwx.basemodule.databinding.FragmentMediaPlayerTestBinding

class MediaPlayerTestFragment :
    BaseBindingFragment<FragmentMediaPlayerTestBinding>(R.layout.fragment_media_player_test) {

    private val player by lazy { LjwxMediaPlayer(requireContext(), requireContext().cacheDir.path) }

    private val work = """
        "audiosDict": {
            "b1eee8ea-8043-4bc8-a7c3-c1edfd61fbfa": {
                "id": "b1eee8ea-8043-4bc8-a7c3-c1edfd61fbfa",
                "name": "4",
                "url": "https://creation.codemao.cn/396/f71c08153df54dc1b7e35edca840ccbd.mp3"
            },
            "dd6290ea-1f55-4869-a249-10f951fa2b95": {
                "id": "dd6290ea-1f55-4869-a249-10f951fa2b95",
                "name": "3",
                "url": "https://creation.codemao.cn/396/5482eefa68cf4eb28311cf6d65388626.mp3"
            },
            "900d8249-ae52-4a1d-bde6-41afd0cf0d21": {
                "id": "900d8249-ae52-4a1d-bde6-41afd0cf0d21",
                "name": "1",
                "url": "https://dev-cdn-common.codemao.cn/dev/922/user-files/FuyU2UbsBdx5uc4v2joUICeEoXyW.mp3"
            },
            "32fd9c39-5327-4978-915b-8406a1079c6e": {
                "id": "32fd9c39-5327-4978-915b-8406a1079c6e",
                "name": "2",
                "url": "https://dev-cdn-common.codemao.cn/dev/922/user-files/FlJkGBFXTSwVv_pcXJJhK9G1uOGs.mp3"
            },
            "8f778b84-7300-4a2b-b9df-c7dfef5d5bc0": {
                "id": "8f778b84-7300-4a2b-b9df-c7dfef5d5bc0",
                "name": "5",
                "url": "https://dev-cdn-common.codemao.cn/dev/922/user-files/Fong3rUX3O9cmgYNadpgAr3UTeI6.mp3"
            }
        },
    """.trimIndent()

    private val video = """
        https://v95-web-sz.douyinvod.com/283cd63fcbdaa25f15f985a1a53646fc/66704433/video/tos/cn/tos-cn-ve-15c001-alinc2/oEvprdZKOtw3PIQQul517RhAAivAmBHiEIqpn/?a=6383&ch=5&cr=3&dr=0&lr=all&cd=0%7C0%7C0%7C3&cv=1&br=889&bt=889&cs=0&ds=4&ft=GZnU0RqeffPdXP~ka1zNvAq-antLjrK.-w3.Rka1-PPVvjVhWL6&mime_type=video_mp4&qs=0&rc=aWY6ZDxpNzRmOGY5aDpnPEBpM2R4eHE5cmdvczMzNGkzM0AyYWM0LzJeXmIxLzJeX2AuYSNpanBuMmRrb2hgLS1kLTBzcw%3D%3D&btag=c0000e00030000&cquery=100H_100K_100o_101s_100B&dy_q=1718622339&feature_id=46a7bb47b4fd1280f3d3825bf2b29388&l=20240617190538C674D507D7FB2217EA7A
    """.trimIndent()

    private val audio = """
        https://dev-cdn-common.codemao.cn/dev/922/user-files/Fong3rUX3O9cmgYNadpgAr3UTeI6.mp3
    """.trimIndent()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.playerView.setLjwxPlayer(player)

        mBinding.playLink.setText(video)

        player.addListener(object : LjwxMediaPlayerListener {
            override fun onStateChange(state: Int, message: String) {
                Log.d("播放状态改变", "描述:$message")
            }

        })

        mBinding.play.singleClick {
            val path = mBinding.playLink.text.toString()
            player.start(path)
        }

        mBinding.pause.singleClick {
            player.pause()
        }

        mBinding.release.singleClick {
            player.release()
        }

        var video1 = true
        mBinding.audio.singleClick {
            if (video1) {
                mBinding.playLink.setText(audio)
                video1 = false
            } else {
                mBinding.playLink.setText(video)
                video1 = true
            }
        }

    }

}