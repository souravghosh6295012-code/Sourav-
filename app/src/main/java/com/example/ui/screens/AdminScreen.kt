package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import com.example.util.DownloadHelper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Album
import com.example.data.model.AppConfig
import com.example.data.model.MediaItem
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun AdminScreen(
    allMedia: List<MediaItem>,
    allAlbums: List<Album>,
    config: AppConfig,
    isUploading: Boolean,
    uploadProgress: Pair<Int, Int>?,
    onExitAdmin: () -> Unit,
    onUpload: (
        uris: List<Uri>,
        isVideo: Boolean,
        category: String,
        albumId: String,
        albumName: String,
        clientName: String,
        title: String,
        description: String,
        allowDownload: Boolean,
        onComplete: (Int) -> Unit
    ) -> Unit,
    onDeleteMedia: (MediaItem) -> Unit,
    onToggleVisibility: (MediaItem) -> Unit,
    onToggleDownloadPermission: (MediaItem) -> Unit,
    onCreateAlbum: (title: String, category: String, coverUri: String, description: String, clientName: String, date: String) -> Unit,
    onDeleteAlbum: (Album) -> Unit,
    onUpdateSettings: (brand: String, phone: String, heroTitle: String, heroSubtitle: String, globalDownloads: Boolean, newPin: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onExitAdmin() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upload Media", "Manage Media", "Manage Albums", "App Settings")

    // Upload Form State
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isVideoUpload by remember { mutableStateOf(false) }
    var uploadCategory by remember { mutableStateOf("Wedding") }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadClientName by remember { mutableStateOf("") }
    var uploadDescription by remember { mutableStateOf("") }
    var selectedAlbumId by remember { mutableStateOf("") }
    var allowVisitorsDownload by remember { mutableStateOf(true) }

    // Multi Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = uris
            isVideoUpload = false
            Toast.makeText(context, "${uris.size} photo(s) selected from phone gallery", Toast.LENGTH_SHORT).show()
        }
    }

    // Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUris = listOf(uri)
            isVideoUpload = true
            Toast.makeText(context, "Video selected from phone gallery", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackSurface)
    ) {
        // Admin Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlackSurfaceElevated)
                .border(width = 0.5.dp, color = BlackBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldDark.copy(alpha = 0.3f))
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "OWNER ADMIN PANEL",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Private Management & Uploads",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onExitAdmin,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("admin_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Exit Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BlackSurface,
            contentColor = GoldPrimary,
            edgePadding = 12.dp,
            divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BlackBorder)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) GoldPrimary else TextMuted
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Tab 1: Upload Media from Phone Gallery
                UploadMediaTab(
                    selectedUris = selectedUris,
                    isVideo = isVideoUpload,
                    category = uploadCategory,
                    title = uploadTitle,
                    clientName = uploadClientName,
                    description = uploadDescription,
                    selectedAlbumId = selectedAlbumId,
                    allowDownload = allowVisitorsDownload,
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    albums = allAlbums,
                    onPickPhotos = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPickVideo = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onClearSelection = { selectedUris = emptyList() },
                    onCategoryChange = { uploadCategory = it },
                    onTitleChange = { uploadTitle = it },
                    onClientNameChange = { uploadClientName = it },
                    onDescriptionChange = { uploadDescription = it },
                    onAlbumChange = { selectedAlbumId = it },
                    onAllowDownloadChange = { allowVisitorsDownload = it },
                    onPublish = {
                        val albumName = allAlbums.find { it.id == selectedAlbumId }?.title ?: ""
                        onUpload(
                            selectedUris,
                            isVideoUpload,
                            uploadCategory,
                            selectedAlbumId,
                            albumName,
                            uploadClientName,
                            uploadTitle,
                            uploadDescription,
                            allowVisitorsDownload
                        ) { count ->
                            Toast.makeText(context, "Successfully published $count items!", Toast.LENGTH_LONG).show()
                            selectedUris = emptyList()
                            uploadTitle = ""
                            uploadDescription = ""
                            uploadClientName = ""
                        }
                    }
                )
            }
            1 -> {
                // Tab 2: Manage Media
                ManageMediaTab(
                    mediaList = allMedia,
                    onDelete = onDeleteMedia,
                    onToggleVisibility = onToggleVisibility,
                    onToggleDownload = onToggleDownloadPermission
                )
            }
            2 -> {
                // Tab 3: Manage Albums
                ManageAlbumsTab(
                    albums = allAlbums,
                    onCreateAlbum = onCreateAlbum,
                    onDeleteAlbum = onDeleteAlbum
                )
            }
            3 -> {
                // Tab 4: App Settings & Branding
                AppSettingsTab(
                    config = config,
                    onSave = onUpdateSettings
                )
            }
        }
    }
}

@Composable
fun UploadMediaTab(
    selectedUris: List<Uri>,
    isVideo: Boolean,
    category: String,
    title: String,
    clientName: String,
    description: String,
    selectedAlbumId: String,
    allowDownload: Boolean,
    isUploading: Boolean,
    uploadProgress: Pair<Int, Int>?,
    albums: List<Album>,
    onPickPhotos: () -> Unit,
    onPickVideo: () -> Unit,
    onClearSelection: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onClientNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAlbumChange: (String) -> Unit,
    onAllowDownloadChange: (Boolean) -> Unit,
    onPublish: () -> Unit
) {
    val context = LocalContext.current
    val categories = listOf("Wedding", "Birthday", "Annaprashan", "Baby", "Portrait", "Nature")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Step 1: Select Media from Phone
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Connect to Phone Gallery",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Pick high-resolution photos or MP4 video files directly from your phone's storage.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPickPhotos,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("admin_pick_photos_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Photos", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onPickVideo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldDark,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("admin_pick_video_button")
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Video", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Selected Preview
                    if (selectedUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedUris.size} ${if (isVideo) "video" else "photo(s)"} ready to publish",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear",
                                color = ErrorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onClearSelection() }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Selected media",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isVideo) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Media Details
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Details & Category",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Category:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GoldPrimary else BlackSurfaceElevated)
                                    .border(1.dp, if (isSelected) GoldPrimary else BlackBorder, RoundedCornerShape(16.dp))
                                    .clickable { onCategoryChange(cat) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Title (e.g., Royal Reception Sindoor Daan)", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = onClientNameChange,
                        label = { Text("Client / Couple Name (e.g., Rahul & Priya)", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_client_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Description / Story", color = GoldLight) },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_desc_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Assign Album
                    if (albums.isNotEmpty()) {
                        Text("Assign to Album (Optional):", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(albums) { album ->
                                val isSelected = selectedAlbumId == album.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) GoldPrimary else BlackSurfaceElevated)
                                        .border(1.dp, if (isSelected) GoldPrimary else BlackBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onAlbumChange(if (isSelected) "" else album.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = album.title,
                                        color = if (isSelected) Color.Black else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Permissions & Publish
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Permissions & Publishing",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow Visitors to Download",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "When enabled, clients can save this media to their phone gallery.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = allowDownload,
                            onCheckedChange = onAllowDownloadChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GoldPrimary,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BlackSurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isUploading && uploadProgress != null) {
                        LinearProgressIndicator(
                            progress = { uploadProgress.first.toFloat() / uploadProgress.second.toFloat().coerceAtLeast(1f) },
                            color = GoldPrimary,
                            trackColor = BlackSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Importing & saving: ${uploadProgress.first} of ${uploadProgress.second} items...",
                            color = GoldLight,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = onPublish,
                        enabled = selectedUris.isNotEmpty() && !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_publish_button")
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publishing...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedUris.isEmpty()) "Select Media First" else "Publish to Bright Digital",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageMediaTab(
    mediaList: List<MediaItem>,
    onDelete: (MediaItem) -> Unit,
    onToggleVisibility: (MediaItem) -> Unit,
    onToggleDownload: (MediaItem) -> Unit
) {
    val context = LocalContext.current

    if (mediaList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No media items found", color = GoldLight)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(mediaList, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, BlackBorder, RoundedCornerShape(8.dp))
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
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${item.category} • ${item.mediaType}",
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${if (item.isPublic) "Public" else "Hidden"} • ${if (item.allowDownload) "Download ON" else "Download OFF"}",
                            color = if (item.isPublic) SuccessGreen else TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    // Action Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Toggle Visibility
                        IconButton(
                            onClick = { onToggleVisibility(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isPublic) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = if (item.isPublic) GoldPrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Toggle Download Permission
                        IconButton(
                            onClick = { onToggleDownload(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Toggle Download Permission",
                                tint = if (item.allowDownload) SuccessGreen else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = { onDelete(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Item",
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageAlbumsTab(
    albums: List<Album>,
    onCreateAlbum: (title: String, category: String, coverUri: String, description: String, clientName: String, date: String) -> Unit,
    onDeleteAlbum: (Album) -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }

    var albumTitle by remember { mutableStateOf("") }
    var albumCategory by remember { mutableStateOf("Wedding") }
    var albumClientName by remember { mutableStateOf("") }
    var albumDate by remember { mutableStateOf("") }
    var albumCoverUri by remember { mutableStateOf("") }
    var albumDesc by remember { mutableStateOf("") }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            albumCoverUri = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Create Album Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create New Album",
                            color = GoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showCreateDialog = !showCreateDialog },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showCreateDialog) "Close" else "New Album", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (showCreateDialog) {
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = albumTitle,
                            onValueChange = { albumTitle = it },
                            label = { Text("Album Title", color = GoldLight) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = BlackBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = albumClientName,
                            onValueChange = { albumClientName = it },
                            label = { Text("Client Name", color = GoldLight) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = BlackBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = albumDate,
                            onValueChange = { albumDate = it },
                            label = { Text("Event Date (e.g., November 2026)", color = GoldLight) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = BlackBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                coverPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackSurfaceElevated,
                                contentColor = GoldLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (albumCoverUri.isNotBlank()) "Cover Photo Selected ✓" else "Pick Cover Photo from Gallery")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (albumTitle.isNotBlank()) {
                                    onCreateAlbum(
                                        albumTitle,
                                        albumCategory,
                                        albumCoverUri,
                                        albumDesc,
                                        albumClientName,
                                        albumDate
                                    )
                                    albumTitle = ""
                                    albumClientName = ""
                                    albumDate = ""
                                    albumCoverUri = ""
                                    showCreateDialog = false
                                    Toast.makeText(context, "Album created successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = albumTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Create Album", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Existing Albums List
        items(albums, key = { it.id }) { album ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(album.coverUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.title,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${album.category} • ${album.clientName}",
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { onDeleteAlbum(album) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Album",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSettingsTab(
    config: AppConfig,
    onSave: (brand: String, phone: String, heroTitle: String, heroSubtitle: String, globalDownloads: Boolean, newPin: String) -> Unit
) {
    val context = LocalContext.current

    var brandName by remember { mutableStateOf(config.brandName) }
    var contactPhone by remember { mutableStateOf(config.contactPhone) }
    var heroTitle by remember { mutableStateOf(config.heroTitle) }
    var heroSubtitle by remember { mutableStateOf(config.heroSubtitle) }
    var globalDownloads by remember { mutableStateOf(config.globalDownloadsEnabled) }
    var newPin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Branding & Details",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Brand Name (strictly Bright Digital)", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Call Us Phone Number", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = heroTitle,
                        onValueChange = { heroTitle = it },
                        label = { Text("Hero Showcase Title", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = heroSubtitle,
                        onValueChange = { heroSubtitle = it },
                        label = { Text("Hero Subtitle", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Global Download Permission
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global Visitor Downloads",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Allow public visitors to download photos and videos to their phones.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = globalDownloads,
                        onCheckedChange = { globalDownloads = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldPrimary
                        )
                    )
                }
            }
        }

        // Change Owner PIN
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security & Owner PIN",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Change the passcode used to unlock the owner admin panel (Default: 8327).",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 8) newPin = it },
                        placeholder = { Text("Leave blank to keep current PIN (${config.ownerPin})", color = TextMuted) },
                        label = { Text("New 4-8 Digit PIN", color = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Share App & Installation Guide for Customer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Install & Share with Customers",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Share this app link with your customers so they can view and download their photos & videos directly on their phone:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BlackSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "https://ais-pre-4hlz4vglxw3wcuuqcgno7s-677191090748.asia-east1.run.app",
                            color = GoldLight,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                DownloadHelper.openWhatsApp(
                                    context,
                                    "Hello! Welcome to BRIGHT DIGITAL Photography & Videography. Open our app on your phone here to view your photos & videos:\nhttps://ais-pre-4hlz4vglxw3wcuuqcgno7s-677191090748.asia-east1.run.app"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                DownloadHelper.shareMedia(
                                    context,
                                    "BRIGHT DIGITAL Mobile App",
                                    "https://ais-pre-4hlz4vglxw3wcuuqcgno7s-677191090748.asia-east1.run.app",
                                    false
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Share Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    onSave(brandName, contactPhone, heroTitle, heroSubtitle, globalDownloads, newPin)
                    Toast.makeText(context, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_save_settings_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
