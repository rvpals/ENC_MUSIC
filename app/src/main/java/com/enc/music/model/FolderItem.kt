package com.enc.music.model

data class FolderItem(
    val name: String,
    val path: String,
    val songCount: Int,
    val subfolderCount: Int
)
