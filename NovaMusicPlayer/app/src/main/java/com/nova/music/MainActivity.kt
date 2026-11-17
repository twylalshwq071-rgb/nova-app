package com.nova.music

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.launch

private val Ink = Color(0xFF090B12)
private val Panel = Color(0xFF151A26)
private val Accent = Color(0xFF9B8CFF)
private val Muted = Color(0xFF9AA3B5)

class MainActivity : ComponentActivity() {
    private val repository by lazy { AudioRepository(this) }
    private var tracks by mutableStateOf<List<Track>>(emptyList())
    private var controller: MediaController? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) scanLibrary() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectController()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Ink, surface = Panel, primary = Accent,
                    onBackground = Color.White, onSurface = Color.White
                )
            ) {
                LibraryScreen(
                    tracks = tracks,
                    onPlay = { index -> playAt(index) },
                    onRefresh = { requestAudioPermission() }
                )
            }
        }
        requestAudioPermission()
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            scanLibrary()
        } else permissionLauncher.launch(permission)
    }

    private fun scanLibrary() {
        lifecycleScope.launch { tracks = repository.scan() }
    }

    private fun connectController() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({ controller = future.get() }, ContextCompat.getMainExecutor(this))
    }

    private fun playAt(index: Int) {
        val c = controller ?: return
        val items = tracks.map { track ->
            MediaItem.Builder().setUri(track.uri).setMediaId(track.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(track.title).setArtist(track.artist).setAlbumTitle(track.album).build()
                ).build()
        }
        c.setMediaItems(items, index, 0L)
        c.prepare()
        c.play()
    }

    override fun onDestroy() {
        controller?.release()
        super.onDestroy()
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<Track>,
    onPlay: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var tab by remember { mutableStateOf("Songs") }
    var query by remember { mutableStateOf("") }
    val tabs = listOf("Songs", "Playlists", "Albums", "Artists", "Folders")
    val filtered = tracks.filter {
        it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true)
    }

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = Accent) {
                listOf(Icons.Default.MusicNote, Icons.Default.QueueMusic, Icons.Default.Album,
                    Icons.Default.Person, Icons.Default.Folder).forEachIndexed { index, icon ->
                    NavigationBarItem(
                        selected = tabs[index] == tab,
                        onClick = { tab = tabs[index] },
                        icon = { Icon(icon, contentDescription = tabs[index]) },
                        label = { Text(tabs[index], fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(Ink).padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Text("NOVA", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("Your sound. Your space.", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your library", color = Muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent, unfocusedBorderColor = Panel,
                    focusedContainerColor = Panel, unfocusedContainerColor = Panel
                )
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(tab, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${filtered.size} tracks", color = Muted, fontSize = 12.sp)
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Rescan", tint = Accent) }
            }
            if (tracks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LibraryMusic, null, tint = Accent, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Your music library is waiting", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Allow audio access, then rescan your device.", color = Muted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { track ->
                        val index = tracks.indexOf(track)
                        Row(
                            Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp))
                                .clickable { onPlay(index) }.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(46.dp).background(Color(0xFF28233F), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.MusicNote, null, tint = Accent) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("${track.artist}  •  ${track.album}", color = Muted, fontSize = 11.sp, maxLines = 1)
                            }
                            Text(track.durationLabel, color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
