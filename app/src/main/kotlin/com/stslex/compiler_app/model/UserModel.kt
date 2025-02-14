package com.stslex.compiler_app.model

data class UserModel(
    val name: String,
    val secondName: String,
) {

    fun getChangesValue(
        user: UserModel?
    ): String = if (user == null) {
        "User full change"
    } else {
        val changes = mutableListOf<String>()
        if (user.name != name) changes.add("name")
        if (user.secondName != secondName) changes.add("secondName")
        when {
            changes.isEmpty() -> "No changes"
            changes.size == 7 -> "User full change"
            else -> changes.joinToString(", ") + " changed"
        }
    }

    companion object {

        val defaultMock = UserModel(
            name = "Test User",
            secondName = "Test SecondName",
        )
    }
}