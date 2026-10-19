package com.example.melody

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

private data class Song(val id: Long, val title: String, val artist: String, val uri: android.net.Uri)

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private val songs = mutableStateListOf<Song>()
    private var permissionGranted by mutableStateOf(false)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (granted) loadSongs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        permissionGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) loadSongs() else permissionLauncher.launch(permission)
        setContent {
            val dark = Color(0xFF111116); val panel = Color(0xFF202027); val purple = Color(0xFF9B87FF)
            MaterialTheme(colorScheme = darkColorScheme(primary = purple, background = dark, surface = panel)) {
                var playing by remember { mutableStateOf(false) }
                var current by remember { mutableIntStateOf(-1) }
                Surface(Modifier.fillMaxSize(), color = dark) {
                    Column(Modifier.fillMaxSize().padding(20.dp)) {
                        Text("MELODY", color = purple, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(18.dp))
                        Text("Your music.\nYour way.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Offline music player", color = Color.LightGray)
                        Spacer(Modifier.height(22.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Your library", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), color = Color.White)
                            Text("${songs.size} tracks", color = Color.LightGray)
                        }
                        Spacer(Modifier.height(10.dp))
                        if (!permissionGranted) Text("Allow audio access to show music on your device.", color = Color.LightGray)
                        else if (songs.isEmpty()) Text("No songs found yet. Add audio files to your phone's music folder.", color = Color.LightGray)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(songs) { index, song ->
                                Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(14.dp)).clickable {
                                    current = index
                                    player?.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, index, 0L)
                                    player?.prepare(); player?.play(); playing = true
                                }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).background(Color(0xFF343044), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("♫", color = purple, style = MaterialTheme.typography.titleLarge) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(song.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text(song.artist, color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    }
                                    if (current == index && playing) Text("♫", color = purple)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (current in songs.indices) songs[current].title else "Nothing playing", color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(if (current in songs.indices) songs[current].artist else "Choose a track above", color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Button(onClick = {
                                if (current < 0 && songs.isNotEmpty()) {
                                    current = 0; player?.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }); player?.prepare()
                                }
                                if (player?.isPlaying == true) { player?.pause(); playing = false }
                                else if (songs.isNotEmpty()) { player?.play(); playing = true }
                            }, enabled = songs.isNotEmpty()) { Text(if (playing) "Pause" else "Play") }
                            Spacer(Modifier.width(6.dp))
                            Button(onClick = {
                                if (songs.isNotEmpty()) { current = if (current + 1 >= songs.size) 0 else current + 1; player?.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, current, 0L); player?.prepare(); player?.play(); playing = true }
                            }, enabled = songs.isNotEmpty()) { Text("Next") }
                        }
                    }
                }
            }
        }
    }

    private fun loadSongs() {
        songs.clear()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST)
        contentResolver.query(collection, projection, "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                songs.add(Song(id, cursor.getString(titleCol) ?: "Unknown title", cursor.getString(artistCol) ?: "Unknown artist", ContentUris.withAppendedId(collection, id)))
            }
        }
    }

    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
