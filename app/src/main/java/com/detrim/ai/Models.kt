package com.detrim.ai

enum class DetrimModel(val displayName: String, val cost: Int) {
    FAST("Detrim 1.0 Fast", 5),
    PRO("Detrim 2.0", 10)
}

enum class AspectRatioOption(val label: String) {
    PORTRAIT_9_16("9:16"),
    LANDSCAPE_16_9("16:9")
}

