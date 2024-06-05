package com.example.taichungtourism

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class FileHelper(private val context: Context) {
    private val fileName = "collections.json"
    private val gson = Gson()

    // 讀取所有資料
    fun readAll(): List<Attraction> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }

        FileReader(file).use { reader ->
            val type = object : TypeToken<List<Attraction>>() {}.type
            return gson.fromJson(reader, type)
        }
    }

    // 保存單筆資料
    fun save(attraction: Attraction) {
        val currentData = readAll().toMutableList()
        currentData.add(attraction)
        writeToFile(currentData)
    }

    // 刪除單筆資料
    fun delete(name: String) {
        val currentData = readAll().toMutableList()
        val iterator = currentData.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == name) {
                iterator.remove()
                break
            }
        }
        writeToFile(currentData)
    }

    // 將資料寫入文件
    private fun writeToFile(data: List<Attraction>) {
        val file = File(context.filesDir, fileName)
        FileWriter(file).use { writer ->
            gson.toJson(data, writer)
        }
    }
}