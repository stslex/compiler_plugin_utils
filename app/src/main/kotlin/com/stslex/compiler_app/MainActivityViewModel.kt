package com.stslex.compiler_app

import androidx.lifecycle.ViewModel
import com.stslex.compiler_app.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {

    private val _userInfo = MutableStateFlow(UserModel.defaultMock)

    val userInfo: StateFlow<UserModel>
        get() = _userInfo.asStateFlow()

    fun setName(name: String) {
        _userInfo.value = _userInfo.value.copy(name = name)
    }

    fun setSecondName(secondName: String) {
        _userInfo.value = _userInfo.value.copy(secondName = secondName)
    }

}