package ua.skomua

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

@CloudstreamPlugin
class SkomUAPlugin : Plugin() {
    override fun load(context: Context) { registerMainAPI(SkomUA()) }
}

data class PlayerNode(
    val title: String = "",
    val file: String? = null,
    val folder: List<PlayerNode>? = null,
    val poster: String? = null,
    val subtitle: String? = null,
)
data class Playback(val urls: List<String>, val referer: String, val subtitles: String? = null)

class SkomUA : MainAPI() {
    override var mainUrl = "https://skomua.online"
    override var name = "SkomUA"
    override var lang = "uk"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    private val gson = Gson()
    override val mainPage = mainPageOf(
        "$mainUrl/redubs" to "Переозвучки",
        "$mainUrl/films" to "Фільми",
        "$mainUrl/series" to "Серіали",
        "$mainUrl/standup" to "Стендапи",
    )

    private fun Element.card(): SearchResponse? {
        val title = selectFirst(".card__title")?.text()?.takeIf { it.isNotBlank() } ?: return null
        val url = fixUrl(attr("href"))
        val poster = selectFirst("img")?.attr("src")?.let { fixUrl(it) }
        return if (url.contains("/series/")) newTvSeriesSearchResponse(title, url, TvType.TvSeries) { posterUrl = poster }
        else newMovieSearchResponse(title, url, TvType.Movie) { posterUrl = poster }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (page > 1) return newHomePageResponse(request.name, emptyList(), false)
        val cards = app.get(request.data).document.select("a.card").mapNotNull { it.card() }.distinctBy { it.url }
        return newHomePageResponse(request.name, cards, false)
    }

    override suspend fun search(query: String): List<SearchResponse> =
        app.get(mainUrl, params = mapOf("q" to query)).document.select("a.card")
            .mapNotNull { it.card() }.distinctBy { it.url }

    private fun playerFile(html: String): String? =
        Regex("""\bfile\s*:\s*'((?:\\.|[^'\\])*)'""").findAll(html).lastOrNull()?.groupValues?.get(1)
            ?: Regex("\\bfile\\s*:\\s*\"([^\"]+)\"").findAll(html).lastOrNull()?.groupValues?.get(1)

    private fun decodeJs(value: String): String = value.replace("\\/", "/").replace("\\'", "'")
    private fun encode(playback: Playback): String = gson.toJson(playback)

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.watch-page__title")?.text() ?: throw ErrorLoadingException("Не знайдено назву")
        val poster = doc.selectFirst("meta[property=og:image]")?.attr("content")?.let { fixUrl(it) }
        val description = doc.selectFirst(".watch-page__desc")?.text()
        val embeds = doc.select(".player iframe").mapNotNull {
            it.attr("data-src").ifBlank { it.attr("src") }.takeIf { link -> link.isNotBlank() }?.let { link -> fixUrl(link) }
        }.distinct()
        if (!url.contains("/series/")) return newMovieLoadResponse(title, url, TvType.Movie, encode(Playback(embeds, url))) {
            posterUrl = poster; plot = description
        }
        val episodes = mutableListOf<Episode>()
        for (embed in embeds.filter { java.net.URI(it).host?.endsWith("ashdi.vip") == true }) {
            val html = app.get(embed, referer = url).text
            val file = playerFile(html)?.let(::decodeJs) ?: continue
            if (!file.trimStart().startsWith("[")) continue
            val nodes: List<PlayerNode> = gson.fromJson(file, object : TypeToken<List<PlayerNode>>() {}.type)
            fun walk(items: List<PlayerNode>, season: Int? = null) {
                items.forEach { node ->
                    val seasonNumber = Regex("""(?i)(?:сезон|season)\s*(\d+)""").find(node.title)?.groupValues?.get(1)?.toIntOrNull() ?: season
                    if (node.folder != null) walk(node.folder, seasonNumber)
                    else if (!node.file.isNullOrBlank()) {
                        episodes += newEpisode(encode(Playback(listOf(node.file), embed, node.subtitle))) {
                            name = node.title.trim()
                            this.season = seasonNumber
                            episode = Regex("""\d+""").find(node.title)?.value?.toIntOrNull()
                            posterUrl = node.poster
                        }
                    }
                }
            }
            walk(nodes)
            if (episodes.isNotEmpty()) break
        }
        if (episodes.isEmpty()) throw ErrorLoadingException("Ashdi не повернув список серій. Спробуйте пізніше.")
        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) { posterUrl = poster; plot = description }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val playback = gson.fromJson(data, Playback::class.java)
        var found = false
        suspend fun emit(file: String, referer: String) {
            val variants = Regex("""(?:\[([^]]+)])?(https?://[^\s,\[\]]+)""").findAll(file).toList()
            for (variant in variants) {
                val link = variant.groupValues[2]
                if (!link.contains(".m3u8") && !link.contains(".mp4")) continue
                val hls = link.contains(".m3u8")
                callback(newExtractorLink(name, "$name ${variant.groupValues[1]}".trim(), link,
                    if (hls) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO) { this.referer = referer })
                found = true
            }
        }
        playback.subtitles?.let { subs ->
            Regex("""\[([^]]+)](https?://[^,]+)""").findAll(subs).forEach {
                subtitleCallback(SubtitleFile(it.groupValues[1], it.groupValues[2]))
            }
        }
        for (url in playback.urls) {
            if (url.contains(".m3u8") || url.contains(".mp4")) { emit(url, playback.referer); continue }
            if (java.net.URI(url).host?.endsWith("ashdi.vip") == true) {
                val file = playerFile(app.get(url, referer = playback.referer).text)?.let(::decodeJs)
                if (file != null && !file.trimStart().startsWith("[")) emit(file, url)
            } else {
                loadExtractor(url, playback.referer, subtitleCallback) { callback(it); found = true }
            }
        }
        return found
    }
}
