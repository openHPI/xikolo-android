package de.xikolo.testing.instrumented.mocking

import de.xikolo.models.VideoStream

object SingleObjects {

    const val testVideoStreamHdUrl: String = "https://player.vimeo.com/external/334434941.hd.mp4?s=e82a14998ec2b2beb1a5b2afd003ec4e2d7a0942&profile_id=175&oauth2_token_id=1212580277"
    const val testVideoStreamSdUrl: String = "https://player.vimeo.com/external/334434941.sd.mp4?s=67045e5677569bfab51c45c518f92d54f4f27475&profile_id=164&oauth2_token_id=1212580277"
    const val testVideoStreamHlsUrl: String = "https://open.sap.com/playlists/9c1ef86c-e160-4ec0-869e-dd06c05017f0.m3u8"
    const val testVideoStreamHdSize: Int = 43239647
    const val testVideoStreamSdSize: Int = 7746575
    const val testVideoStreamThumbnailUrl: String = "https://i.vimeocdn.com/video/781666885_1920x1080.jpg?r=pad"

    val testVideoStream = VideoStream(
        testVideoStreamHdUrl,
        testVideoStreamSdUrl,
        testVideoStreamHlsUrl,
        testVideoStreamHdSize,
        testVideoStreamSdSize,
        testVideoStreamThumbnailUrl
    )

}