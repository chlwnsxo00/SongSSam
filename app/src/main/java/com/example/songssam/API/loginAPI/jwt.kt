package com.example.songssam.API.loginAPI

import com.google.gson.annotations.SerializedName



data class jwt(
    @SerializedName("response")val jwt : tokens
)

data class tokens(
    @SerializedName("accessToken")val accessToken : String,
    @SerializedName("refreshToken")val refreshToken : String
)

data class user(
    @SerializedName("HttpStatus")val status:Long,
    @SerializedName("response")val userinfo : userInfo
)

data class userInfo(
    @SerializedName("id")val id : Long,
    @SerializedName("email")val email : String,
    @SerializedName("nickname")val nickname : String,
    @SerializedName("profileUrl")val profile : String,
    @SerializedName("role")val role : String
)