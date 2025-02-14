package com.stslex.compiler_app

import android.content.Context
import android.widget.Toast
import com.stslex.compiler_app.model.UserModel

object UserToastUtil {

    private var user: UserModel? = null

    fun Context.sendToastOfUserChanges(
        userModel: UserModel
    ) {
        val textMsg = userModel.getChangesValue(user)
        Toast.makeText(this, textMsg, Toast.LENGTH_SHORT).show()
        user = userModel
    }
}