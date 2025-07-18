package com.example.tftitemapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.*
import org.json.JSONArray
import java.util.Objects


class ItemDataFromAPI(api: String) {
    // With the API I have, you can only ping
    // the Riot api 100 times every 2 minutes
    private var numOfCalls = 0
    private val key = api
    
    private suspend fun getPuuids(): List<String> = withContext(Dispatchers.IO)  {
        val puuidList = ArrayList<String>()
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://na1.api.riotgames.com/tft/league/v1/challenger")
            .header("X-Riot-Token", key)
            .get()
            .build()

        val response = client.newCall(request).execute()
        numOfCalls++

        response.body?.string()?.let { responseBody ->
            if (response.isSuccessful) {
                val obj = JSONObject(responseBody)
                val entries = obj.getJSONArray("entries")

                repeat(minOf(5, entries.length())) { i ->
                    puuidList.add(entries.getJSONObject(i).getString("puuid"))
                }
            } else {
                println("Request failed with status code: ${response.code}")
                println(responseBody)
            }
        }

        puuidList
    }

    private suspend fun getMatches(): List<String> = withContext(Dispatchers.IO) {
        val puuidList = getPuuids()
        val matchesList = ArrayList<String>()

        for(item in puuidList) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://americas.api.riotgames.com/tft/match/v1/matches/by-puuid/$item/ids")
                .header("X-Riot-Token", key)
                .get()
                .build()

            val response = client.newCall(request).execute()
            numOfCalls++

            response.body?.string()?.let { responseBody ->
                if (response.isSuccessful) {
                    val array = JSONArray(responseBody)
                    repeat(array.length()) { i ->
                        matchesList.add(array.getString(i))
                    }
                } else {
                    println("Request failed with status code: ${response.code}")
                    println(responseBody)
                }
            }
        }

        matchesList
    }

    private suspend fun getItemData(): List<Pair<String, Double>> = withContext(Dispatchers.IO) {
        val listOfMatches = getMatches()
        val itemList = ArrayList<Pair<String, Double>>()
        var index = 0

        while (numOfCalls < 100) {
            println(numOfCalls)

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://americas.api.riotgames.com/tft/match/v1/matches/${listOfMatches[index++]}")
                .header("X-Riot-Token", key)
                .get()
                .build()

            val response = client.newCall(request).execute()
            numOfCalls++

            if (response.isSuccessful) {
                val obj = JSONObject(response.body!!.string())
                val infoObject = obj.getJSONObject("info")

                if (infoObject.getInt("queueId") != 1100 && !Objects.equals(infoObject.getString("tft_set_core_name"), "TFTSet14")) {
                    continue
                }

                val participantsArray = infoObject.getJSONArray("participants")

                participantsArray.asSequence().forEach { jsonObject ->
                    val placement = jsonObject.getDouble("placement")
                    val seenItems = mutableSetOf<String>()

                    jsonObject.getJSONArray("units")
                        .asSequence()
                        .flatMap { it.getJSONArray("itemNames").getStrings() }
                        .filter { !TftItemsUtil.isExcludedItem(it) && seenItems.add(it) }
                        .forEach { itemList.add(it to placement) }
                }
            } else {
                println("Request failed with status code: ${response.code}")
                println(response.body?.string())
                break
            }
        }

        itemList
    }

    private fun JSONArray.asSequence(): Sequence<JSONObject> =
        (0 until length()).asSequence().map { getJSONObject(it) }

    private fun JSONArray.getStrings(): Sequence<String> =
        (0 until length()).asSequence().map { getString(it) }

    suspend fun getItemPlacements(): MutableMap<String, Double> = withContext(Dispatchers.IO) {
        val listOfItems = getItemData()
        listOfItems.sortedBy { it.first }
        val itemMap: MutableMap<String, Double> = HashMap()

        listOfItems.forEach { pair ->
            if (itemMap.containsKey(pair.first)) {
                itemMap[pair.first] = itemMap[pair.first]!! + pair.second
            } else {
                itemMap[pair.first] = pair.second
            }
        }

        val counts = listOfItems.groupingBy { it.first }.eachCount()

        val updatedMap = itemMap.mapValues { (key, value) ->
            value / counts[key]!!
        }.toMutableMap()

        updatedMap
    }

}