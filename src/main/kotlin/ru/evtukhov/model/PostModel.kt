package ru.evtukhov.model

data class PostModel(
    var id: Long,
    var author: String,
    var content: String,
    var dateStamp: Long = 0,
    var likedByMe: Boolean = false,
    var likedCount: Int = 0,
    var sharedByMe: Boolean = false,
    var sharedCount: Int = 0,
    var commentsByMe: Boolean = false,
    var commentsCount: Int = 0,
    var address: String? = null,
    var lat: Double?  = null,
    var lng: Double? = null,
    var postType: PostType = PostType.POST,
    var videoLink: String? = null,
    var intentLink: String? = null,
    var imageLink: Int? = null,
    val user: UserModel? = null
)