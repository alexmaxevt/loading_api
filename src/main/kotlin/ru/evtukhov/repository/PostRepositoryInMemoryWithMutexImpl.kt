package ru.evtukhov.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.evtukhov.model.PostModel

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> {
        mutex.withLock {
            return items.reversed()
        }
    }

    override suspend fun getById(id: Long): PostModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    // TODO:
                    items[index] = item
                    item
                }
            }
        }
    }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(likedCount = item.likedCount + 1, likedByMe = true)
                    try {
                        items[index] = copy
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        println("size: ${items.size}")
                        println(index)
                    }
                    copy
                }
            }
        }
    }

    override suspend fun commentById(id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(commentsCount = item.commentsCount + 1, commentsByMe = true)
                    try {
                        items[index] = copy
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        println("size: ${items.size}")
                        println(index)
                    }
                    copy
                }
            }
        }
    }

    override suspend fun shareById(id: Long, userId: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copy = item.copy(sharedCount = item.sharedCount+1)
                try {
                    items[index] = copy
                } catch (e: ArrayIndexOutOfBoundsException) {
                    println("size: ${items.size}")
                    println(index)
                }
                copy
            }
        }
    }

    override suspend fun getLastContent(): List<PostModel> {
        try {
            if (items.isEmpty()) {
                return emptyList()
            }
            return getAll().slice(0..4)
        } catch (e: IndexOutOfBoundsException) {
            return getAll()
        }
    }

    override suspend fun getPostsAfter(id: Long): List<PostModel>? {
        val item = getById(id)
        val itemsReversed = getAll()
        return when (val index = itemsReversed.indexOfFirst { it.id == item?.id }) {
            -1 -> null
            0 -> emptyList()
            else -> itemsReversed.slice(0 until index)
        }

    }

    override suspend fun getPostsBefore(id: Long): List<PostModel>? {
        val item = getById(id)
        val itemsReversed = getAll()
        return when (val index = itemsReversed.indexOfFirst { it.id == item?.id }) {
            -1-> null
            (items.size - 1) -> emptyList()
            else -> {
                try {
                    itemsReversed.slice((index + 1)..(index + 5))
                } catch (e: IndexOutOfBoundsException) {
                    itemsReversed.slice((index + 1) until items.size)
                }
            }
        }
    }
}