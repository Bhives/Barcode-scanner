package com.reproflex3.rplink.presentation.model

data class LogoutResponse(
    val shouldLogout: Boolean = false,
    val logoutMessage: String = ""
)