package net.youapps.wallpaper_apis.customjson

import kotlinx.serialization.Serializable
import net.youapps.wallpaper_apis.RetrofitHelper
import net.youapps.wallpaper_apis.Wallpaper
import net.youapps.wallpaper_apis.WallpaperApi
import retrofit2.http.GET
import retrofit2.http.Url

@Serializable
data class JsonWallpaperItem(val title: String? = null, val url: String)

interface CustomJsonService {
    @GET
    suspend fun getJsonFeed(@Url url: String): List<JsonWallpaperItem>
}

class CustomJsonApi : WallpaperApi() {
    override val name = "Custom JSON"
    override val baseUrl = "https://example.com" // Dummy baseUrl since we use @Url
    override val requiresCommunityName = true

    private val api = RetrofitHelper.create<CustomJsonService>(baseUrl)

    override suspend fun getWallpapers(page: Int): List<Wallpaper> {
        if (page != 1) return emptyList()

        val urlString = communityName ?: return emptyList()
        val url = urlString.trim()
        if (url.isEmpty()) return emptyList()

        return try {
            val fullUrl = if (!url.startsWith("http")) "https://$url" else url
            val items = api.getJsonFeed(fullUrl)
            items.map {
                Wallpaper(
                    imgSrc = it.url,
                    title = it.title,
                    author = "Custom JSON"
                )
            }.shuffled()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
