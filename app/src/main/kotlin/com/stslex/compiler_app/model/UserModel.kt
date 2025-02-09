package com.stslex.compiler_app.model

data class UserModel(
    val uuid: String,
    val name: String,
    val avatarUrl: String,
    val email: String,
    val isLiked: Boolean,
    val likes: Int,
    val isSubscribed: Boolean,
    val subscriptions: Int,
) {

    fun getChangesValue(
        user: UserModel?
    ): String = if (user == null) {
        "User full change"
    } else {
        val changes = mutableListOf<String>()
        if (user.name != name) changes.add("name")
        if (user.avatarUrl != avatarUrl) changes.add("avatarUrl")
        if (user.email != email) changes.add("email")
        if (user.isLiked != isLiked) changes.add("isLiked")
        if (user.likes != likes) changes.add("likes")
        if (user.isSubscribed != isSubscribed) changes.add("isSubscribed")
        if (user.subscriptions != subscriptions) changes.add("subscriptions")
        when {
            changes.isEmpty() -> "No changes"
            changes.size == 7 -> "User full change"
            else -> changes.joinToString(", ") + " changed"
        }
    }

    companion object {

        val defaultMock = UserModel(
            uuid = "uuid",
            name = "Test User",
            avatarUrl = "https://images.unsplash.com/photo-1488161628813-04466f872be2?q=80&w=3376&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            email = "email",
            isLiked = false,
            likes = 0,
            isSubscribed = false,
            subscriptions = 0,
        )
    }
}