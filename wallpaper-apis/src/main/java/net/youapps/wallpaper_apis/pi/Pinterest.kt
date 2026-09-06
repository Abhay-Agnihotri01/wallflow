package net.youapps.wallpaper_apis.pi

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface Pinterest {
    @GET
    suspend fun getRssFeed(@Url url: String): ResponseBody
}
