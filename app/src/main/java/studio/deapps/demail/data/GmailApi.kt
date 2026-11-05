package studio.deapps.demail.data

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Read mail + change labels (archive, spam, trash, read/unread). */
const val GMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.modify"

/** Thrown on HTTP 401 – the access token expired and must be requested again. */
class GmailAuthException : Exception("Authorization expired")

object GmailApi {

    private const val BASE = "https://gmail.googleapis.com/gmail/v1/users/me"

    // ------------------------------------------------------------------ public

    suspend fun profileEmail(token: String): String = withContext(Dispatchers.IO) {
        JSONObject(request("GET", "$BASE/profile", token)).optString("emailAddress")
    }

    /** Newest messages from the INBOX label, with sender / subject / snippet / time / unread flag. */
    suspend fun inbox(token: String, max: Int = 30): List<Mail> = withContext(Dispatchers.IO) {
        val list = JSONObject(request("GET", "$BASE/messages?labelIds=INBOX&maxResults=$max", token))
        val ids = list.optJSONArray("messages") ?: return@withContext emptyList()

        coroutineScope {
            (0 until ids.length())
                .map { ids.getJSONObject(it).getString("id") }
                .map { id -> async { fetchMessage(token, id) } }
                .awaitAll()
        }
    }

    /**
     * Archive      = remove INBOX
     * Spam         = add SPAM, remove INBOX
     * Delete       = add TRASH, remove INBOX
     * Read/Unread  = remove / add UNREAD
     */
    suspend fun batchModify(
        token: String,
        ids: List<String>,
        add: List<String>,
        remove: List<String>,
    ) {
        if (ids.isEmpty()) return
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("ids", JSONArray(ids))
                .put("addLabelIds", JSONArray(add))
                .put("removeLabelIds", JSONArray(remove))
                .toString()
            request("POST", "$BASE/messages/batchModify", token, body)
        }
    }

    // ----------------------------------------------------------------- private

    private fun fetchMessage(token: String, id: String): Mail {
        val url = "$BASE/messages/$id?format=metadata" +
            "&metadataHeaders=From&metadataHeaders=Subject"
        return parseMessage(JSONObject(request("GET", url, token)))
    }

    private fun parseMessage(json: JSONObject): Mail {
        val headers = json.optJSONObject("payload")?.optJSONArray("headers") ?: JSONArray()

        fun header(name: String): String {
            for (i in 0 until headers.length()) {
                val h = headers.getJSONObject(i)
                if (h.optString("name").equals(name, ignoreCase = true)) return h.optString("value")
            }
            return ""
        }

        var unread = false
        json.optJSONArray("labelIds")?.let { labels ->
            for (i in 0 until labels.length()) if (labels.getString(i) == "UNREAD") unread = true
        }

        val snippet = Html.fromHtml(json.optString("snippet"), Html.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()

        return Mail(
            id = json.getString("id"),
            sender = parseSender(header("From")),
            subject = header("Subject").ifBlank { "(no subject)" },
            body = snippet,
            time = formatTime(json.optString("internalDate").toLongOrNull() ?: 0L),
            unread = unread,
        )
    }

    /** `"Marko P." <marko@x.com>` -> `Marko P.`, `marko@x.com` -> `marko@x.com` */
    private fun parseSender(from: String): String {
        val name = from.substringBefore('<').trim().trim('"').trim()
        if (name.isNotEmpty()) return name
        return from.substringAfter('<').substringBefore('>').ifBlank { from }
    }

    private fun formatTime(millis: Long): String {
        if (millis == 0L) return ""
        val zone = ZoneId.systemDefault()
        val dt = Instant.ofEpochMilli(millis).atZone(zone)
        val today = LocalDate.now(zone)
        val day = dt.toLocalDate()
        return when {
            day == today -> dt.format(DateTimeFormatter.ofPattern("HH:mm"))
            day == today.minusDays(1) -> "Yesterday"
            day.isAfter(today.minusDays(7)) -> dt.format(DateTimeFormatter.ofPattern("EEE"))
            day.year == today.year -> dt.format(DateTimeFormatter.ofPattern("MMM d"))
            else -> dt.format(DateTimeFormatter.ofPattern("d.M.yyyy"))
        }
    }

    private fun request(method: String, url: String, token: String, body: String? = null): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 15_000
            c.readTimeout = 20_000
            c.setRequestProperty("Authorization", "Bearer $token")
            c.setRequestProperty("Accept", "application/json")
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code == 401) throw GmailAuthException()
            if (code !in 200..299) throw IOException("Gmail API $code: ${errorMessage(text)}")
            return text
        } finally {
            c.disconnect()
        }
    }

    private fun errorMessage(text: String): String = try {
        JSONObject(text).getJSONObject("error").getString("message")
    } catch (e: Exception) {
        text.take(200)
    }
}
