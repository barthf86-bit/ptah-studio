package com.ptahstudio.myapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

object NetworkService {
    private const val API_URL = "https://jsonplaceholder.typicode.com/posts"

    suspend fun fetchPosts(): List<Post> = withContext(Dispatchers.IO) {
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            connection.disconnect()

            parsePosts(response.toString())
        } else {
            connection.disconnect()
            throw Exception("HTTP Error: $responseCode")
        }
    }

    private fun parsePosts(jsonString: String): List<Post> {
        val posts = mutableListOf<Post>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            posts.add(
                Post(
                    userId = jsonObject.getInt("userId"),
                    id = jsonObject.getInt("id"),
                    title = jsonObject.getString("title"),
                    body = jsonObject.getString("body")
                )
            )
        }
        return posts
    }
}
