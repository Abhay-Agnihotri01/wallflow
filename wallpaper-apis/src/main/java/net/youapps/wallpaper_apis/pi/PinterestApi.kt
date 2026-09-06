package net.youapps.wallpaper_apis.pi

import com.fleeksoft.ksoup.Ksoup
import net.youapps.wallpaper_apis.RetrofitHelper
import net.youapps.wallpaper_apis.Wallpaper
import net.youapps.wallpaper_apis.WallpaperApi

class PinterestApi : WallpaperApi() {
    override val name = "Pinterest"
    override val baseUrl = "https://www.pinterest.com/"
    override val requiresCommunityName = true

    private val api = RetrofitHelper.create<Pinterest>(baseUrl)

    override suspend fun getWallpapers(page: Int): List<Wallpaper> {
        // RSS feeds don't have standard pagination in this format, only fetch on page 1
        if (page != 1) return emptyList()

        val urlsString = communityName ?: return emptyList()
        val urls = urlsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (urls.isEmpty()) return emptyList()

        val allWallpapers = mutableListOf<Wallpaper>()

        for (url in urls) {
            try {
                // Ensure the URL is absolute
                val fullUrl = if (!url.startsWith("http")) "https://$url" else url
                
                val xml = api.getRssFeed(fullUrl).string()
                val doc = Ksoup.parseXml(xml)
                val items = doc.select("item")

                val wallpapers = items.mapNotNull { item ->
                    val description = Ksoup.parse(item.selectFirst("description")?.text().orEmpty())
                    
                    var imgSrc = description.selectFirst("img")?.attr("src")
                    if (imgSrc == null) return@mapNotNull null
                    
                    // Pinterest thumbnails are typically 236x. Replace with originals for full resolution.
                    imgSrc = imgSrc.replace(Regex("""/\d+x/"""), "/originals/")
                    
                    Wallpaper(
                        imgSrc = imgSrc,
                        title = item.selectFirst("title")?.text(),
                        url = item.selectFirst("link")?.text(),
                        creationDate = item.selectFirst("pubDate")?.text()?.take(16),
                        author = "Pinterest"
                    )
                }
                allWallpapers.addAll(wallpapers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Shuffle so wallpapers from different boards are mixed
        return allWallpapers.shuffled()
    }

    override suspend fun getRandomWallpaperUrl(): String? = getWallpapers(1).randomOrNull()?.imgSrc
}
