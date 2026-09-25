package com.detrim.ai

/**
 * "Генерация" на самом деле просто отдаёт случайное готовое видео из списка.
 * Замени ссылки на свои — прямые mp4-ссылки или файлы в res/raw.
 */
object VideoRepository {

    private val landscapeVideos = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )

    // Отдельного публичного набора вертикальных роликов нет — используем те же,
    // плеер впишет их в вертикальный контейнер. Подставь сюда свои 9:16 видео при желании.
    private val portraitVideos = landscapeVideos

    fun randomVideo(aspect: AspectRatioOption): String {
        val list = if (aspect == AspectRatioOption.PORTRAIT_9_16) portraitVideos else landscapeVideos
        return list.random()
    }
}

