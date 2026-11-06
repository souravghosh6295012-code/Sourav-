package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.BrightDigitalDatabase
import com.example.data.repository.MediaRepository
import com.example.ui.components.AdminAuthDialog
import com.example.ui.components.BrightBottomNav
import com.example.ui.components.BrightTopBar
import com.example.ui.components.FullscreenVideoDialog
import com.example.ui.components.MediaLightbox
import com.example.ui.screens.AboutContactScreen
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GalleryViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BrightDigitalApp()
            }
        }
    }
}

@Composable
fun BrightDigitalApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize Database & Repository
    val database = remember { BrightDigitalDatabase.getDatabase(context, scope) }
    val repository = remember {
        MediaRepository(
            database.mediaDao(),
            database.albumDao(),
            database.configDao()
        )
    }

    val viewModel: GalleryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return GalleryViewModel(repository) as T
            }
        }
    )

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val showPinDialog by viewModel.showPinDialog.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val activeLightboxItem by viewModel.activeLightboxItem.collectAsStateWithLifecycle()
    val activeVideoItem by viewModel.activeVideoItem.collectAsStateWithLifecycle()

    val filteredPhotos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val filteredAlbums by viewModel.filteredAlbums.collectAsStateWithLifecycle()
    val publicVideos by viewModel.publicVideos.collectAsStateWithLifecycle()
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()
    val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val albumSearchQuery by viewModel.albumSearchQuery.collectAsStateWithLifecycle()
    val videoSearchQuery by viewModel.videoSearchQuery.collectAsStateWithLifecycle()
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()

    // Global back handling
    if (currentScreen !is Screen.Home) {
        BackHandler {
            when (currentScreen) {
                is Screen.AlbumDetail -> viewModel.navigateTo(Screen.Albums)
                is Screen.Admin -> viewModel.exitAdmin()
                else -> viewModel.navigateTo(Screen.Home)
            }
        }
    }

    Scaffold(
        topBar = {
            BrightTopBar(
                brandName = appConfig.brandName,
                isAdminUnlocked = isAdminUnlocked,
                onAdminClick = { viewModel.promptAdminLogin() },
                onExitAdmin = { viewModel.exitAdmin() }
            )
        },
        bottomBar = {
            BrightBottomNav(
                currentScreen = currentScreen,
                isAdminUnlocked = isAdminUnlocked,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackSurface)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        featuredPhotos = filteredPhotos,
                        featuredVideos = publicVideos,
                        albums = allAlbums,
                        config = appConfig,
                        onNavigate = { dest -> viewModel.navigateTo(dest) },
                        onCategorySelect = { cat -> viewModel.setCategory(cat) },
                        onPhotoClick = { photo -> viewModel.openLightbox(photo) },
                        onVideoClick = { video -> viewModel.openVideo(video) },
                        onAlbumClick = { albumId -> viewModel.navigateTo(Screen.AlbumDetail(albumId)) }
                    )
                }

                is Screen.Photos -> {
                    PhotosScreen(
                        photos = filteredPhotos,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onCategorySelect = { cat -> viewModel.setCategory(cat) },
                        onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                        onPhotoClick = { photo -> viewModel.openLightbox(photo) }
                    )
                }

                is Screen.Videos -> {
                    VideosScreen(
                        videos = filteredVideos,
                        searchQuery = videoSearchQuery,
                        onSearchQueryChange = { q -> viewModel.setVideoSearchQuery(q) },
                        globalDownloadsEnabled = appConfig.globalDownloadsEnabled,
                        isAdmin = isAdminUnlocked,
                        onVideoPlay = { video -> viewModel.openVideo(video) },
                        onLike = { video -> viewModel.toggleLike(video) }
                    )
                }

                is Screen.Albums -> {
                    AlbumsScreen(
                        albums = filteredAlbums,
                        searchQuery = albumSearchQuery,
                        onSearchQueryChange = { q -> viewModel.setAlbumSearchQuery(q) },
                        onAlbumClick = { albumId -> viewModel.navigateTo(Screen.AlbumDetail(albumId)) }
                    )
                }

                is Screen.AlbumDetail -> {
                    val album = allAlbums.find { it.id == screen.albumId }
                    val albumPhotos = allMedia.filter { it.albumId == screen.albumId }
                    AlbumDetailScreen(
                        album = album,
                        photos = albumPhotos,
                        onBack = { viewModel.navigateTo(Screen.Albums) },
                        onPhotoClick = { photo -> viewModel.openLightbox(photo) }
                    )
                }

                is Screen.About -> {
                    AboutContactScreen(config = appConfig)
                }

                is Screen.Admin -> {
                    AdminScreen(
                        allMedia = allMedia,
                        allAlbums = allAlbums,
                        config = appConfig,
                        isUploading = isUploading,
                        uploadProgress = uploadProgress,
                        onExitAdmin = { viewModel.exitAdmin() },
                        onUpload = { uris, isVideo, cat, albumId, albumName, client, title, desc, allowDl, onComplete ->
                            viewModel.uploadFromGallery(
                                context = context,
                                uris = uris,
                                isVideo = isVideo,
                                category = cat,
                                albumId = albumId,
                                albumName = albumName,
                                clientName = client,
                                title = title,
                                description = desc,
                                allowDownload = allowDl,
                                onComplete = onComplete
                            )
                        },
                        onDeleteMedia = { item -> viewModel.deleteMediaItem(item) },
                        onToggleVisibility = { item -> viewModel.toggleVisibility(item) },
                        onToggleDownloadPermission = { item -> viewModel.toggleDownloadPermission(item) },
                        onCreateAlbum = { title, cat, cover, desc, client, date ->
                            viewModel.createNewAlbum(title, cat, cover, desc, client, date)
                        },
                        onDeleteAlbum = { album -> viewModel.deleteAlbum(album) },
                        onUpdateSettings = { brand, phone, heroTitle, heroSubtitle, dl, pin ->
                            viewModel.updateSettings(brand, phone, heroTitle, heroSubtitle, dl, pin)
                        }
                    )
                }
            }

            // Lightbox Modal
            activeLightboxItem?.let { item ->
                MediaLightbox(
                    item = item,
                    globalDownloadsEnabled = appConfig.globalDownloadsEnabled,
                    isAdmin = isAdminUnlocked,
                    onClose = { viewModel.closeLightbox() },
                    onLike = { photo -> viewModel.toggleLike(photo) }
                )
            }

            // Fullscreen Video Modal
            activeVideoItem?.let { video ->
                FullscreenVideoDialog(
                    item = video,
                    globalDownloadsEnabled = appConfig.globalDownloadsEnabled,
                    isAdmin = isAdminUnlocked,
                    onClose = { viewModel.closeVideo() }
                )
            }

            // Admin Auth Modal
            if (showPinDialog) {
                AdminAuthDialog(
                    errorMessage = pinError,
                    onDismiss = { viewModel.dismissPinDialog() },
                    onVerify = { pin -> viewModel.verifyPin(pin) }
                )
            }
        }
    }
}
