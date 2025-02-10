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

    fun setIsLiked(isLiked: Boolean) {
        _userInfo.value = _userInfo.value.copy(isLiked = isLiked)
    }

    fun setLikes(likes: Int) {
        _userInfo.value = _userInfo.value.copy(likes = likes)
    }

    fun setSubscriptions(subscriptions: Int) {
        _userInfo.value = _userInfo.value.copy(subscriptions = subscriptions)
    }

    fun setIsSubscribed(isSubscribed: Boolean) {
        _userInfo.value = _userInfo.value.copy(isSubscribed = isSubscribed)
    }

    fun setName(name: String) {
        _userInfo.value = _userInfo.value.copy(name = name)
    }

    fun setAvatarUrl(avatarUrl: String) {
        _userInfo.value = _userInfo.value.copy(avatarUrl = avatarUrl)
    }

    fun setEmail(email: String) {
        _userInfo.value = _userInfo.value.copy(email = email)
    }
}