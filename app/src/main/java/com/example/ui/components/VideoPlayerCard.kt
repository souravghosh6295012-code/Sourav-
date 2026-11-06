package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.DownloadHelper
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerCard(
    item: MediaItem,
    globalDownloadsEnabled: Boolean,
    isAdmin: Boolean,
    onPlayClick: () -> Unit,
    onLike: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    val canDownload = (item.allowDownload && globalDownloadsEnabled) || isAdmin

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlackCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail with Play Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val thumb = if (item.thumbnailUri.isNotBlank()) item.thumbnailUri else item.uri
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Play Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.9f))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Category Badge (Top Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category,
                        color = GoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // HD 4K Badge (Top Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CINEMATIC 4K",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Info & Actions Section
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = item.title,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (item.clientName.isNotBlank() || item.shootDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = listOf(item.clientName, item.shootDate)
                            .filter { it.isNotBlank() }
                            .joinToString(" • "),
                        color = GoldLight,
                        fontSize = 12.sp
                    )
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Like button & views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onLike(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = if (item.likesCount > 0) Color(0xFFFF4081) else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${item.likesCount}",
                            color = TextWhite,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${item.viewsCount} views",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Download and Share
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canDownload) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isDownloading = true
                                        DownloadHelper.saveMediaToGallery(
                                            context = context,
                                            uriString = item.uri,
                                            title = item.title,
                                            isVideo = true
                                        )
                                        isDownloading = false
                                    }
                                },
                                enabled = !isDownloading,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldDark.copy(alpha = 0.2f))
                                    .testTag("video_download_btn_${item.id}")
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        color = GoldPrimary,
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download Video",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                DownloadHelper.shareMedia(
                                    context = context,
                                    title = item.title,
                                    uriString = item.uri,
                                    isVideo = true
                                )
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BlackSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = GoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenVideoDialog(
    item: MediaItem,
    globalDownloadsEnabled: Boolean,
    isAdmin: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    val canDownload = (item.allowDownload && globalDownloadsEnabled) || isAdmin

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Android VideoView for smooth native video decoding
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val mediaController = MediaController(ctx)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        setVideoURI(Uri.parse(item.uri))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .align(Alignment.Center)
            )

            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BlackSurface.copy(alpha = 0.8f))
                            .testTag("video_fullscreen_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = item.title,
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = {
                            DownloadHelper.shareMedia(
                                context = context,
                                title = item.title,
                                uriString = item.uri,
                                isVideo = true
                            )
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BlackSurface.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = GoldLight
                        )
                    }
                }
            }

            // Bottom controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                if (canDownload) {
                    Button(
                        onClick = {
                            scope.launch {
                                isDownloading = true
                                DownloadHelper.saveMediaToGallery(
                                    context = context,
                                    uriString = item.uri,
                                    title = item.title,
                                    isVideo = true
                                )
                                isDownloading = false
                            }
                        },
                        enabled = !isDownloading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("video_fullscreen_download")
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving Video to Gallery...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Video to Phone", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
