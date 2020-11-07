package ru.evtukhov.dto

import ru.evtukhov.model.PostType

data class PostRequestDto (
    var id: Long,
    var author: String,
    var content: String,
    var dateStamp: Long,
    var likedByMe: Boolean,
    var likedCount: Int,
    var sharedByMe: Boolean,
    var sharedCount: Int,
    var commentsByMe: Boolean,
    var commentsCount: Int,
    var address: String? = null,
    var lat: Double?  = null,
    var lng: Double? = null,
    var postType: PostType = PostType.POST,
    var videoLink: String? = null,
    var intentLink: String? = null,
    var imageLink: Int? = null
) {

}