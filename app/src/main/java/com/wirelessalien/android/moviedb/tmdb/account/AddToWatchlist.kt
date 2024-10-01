/*
 *     This file is part of "ShowCase" formerly Movie DB. <https://github.com/WirelessAlien/MovieDB>
 *     forked from <https://notabug.org/nvb/MovieDB>
 *
 *     Copyright (C) 2024  WirelessAlien <https://github.com/WirelessAlien>
 *
 *     ShowCase is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ShowCase is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with "ShowCase".  If not, see <https://www.gnu.org/licenses/>.
 */
package com.wirelessalien.android.moviedb.tmdb.account

import android.app.Activity
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.wirelessalien.android.moviedb.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class AddToWatchlist(
    private val movieId: Int,
    private val type: String,
    private val trueOrFalse: Boolean,
    private val activity: Activity
) {
    private val accountId: String?
    private val accessToken: String?
    private val client: OkHttpClient

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        accountId = preferences.getString("account_id", "")
        accessToken = preferences.getString("access_token", "")
        client = OkHttpClient()
    }

    suspend fun addToWatchlist() {
        var success = false
        try {
            val mediaType = MediaType.parse("application/json;charset=utf-8")
            val jsonParam = JSONObject().apply {
                put("media_type", type)
                put("media_id", movieId)
                put("watchlist", trueOrFalse)
            }
            val body = RequestBody.create(mediaType, jsonParam.toString())
            val request = Request.Builder()
                .url("https://api.themoviedb.org/3/account/$accountId/watchlist")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            val responseBody = response.body()!!.string()
            val jsonResponse = JSONObject(responseBody)
            val statusCode = jsonResponse.getInt("status_code")
            success = statusCode == 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val finalSuccess = success
        activity.runOnUiThread {
            if (finalSuccess) {
                Toast.makeText(activity, R.string.added_to_watchlist, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, R.string.removed_from_watchlist, Toast.LENGTH_SHORT).show()
            }
        }
    }
}