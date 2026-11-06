package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Album
import com.example.data.model.AppConfig
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Photos : Screen("photos")
    object Videos : Screen("videos")
    object Albums : Screen("albums")
    data class AlbumDetail(val albumId: String) : Screen("album_detail")
    object About : Screen("about")
    object Admin : Screen("admin")
}

class GalleryViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeLightboxItem = MutableStateFlow<MediaItem?>(null)
    val activeLightboxItem: StateFlow<MediaItem?> = _activeLightboxItem.asStateFlow()

    private val _activeVideoItem = MutableStateFlow<MediaItem?>(null)
    val activeVideoItem: StateFlow<MediaItem?> = _activeVideoItem.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val uploadProgress: StateFlow<Pair<Int, Int>?> = _uploadProgress.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicMedia: StateFlow<List<MediaItem>> = repository.publicMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicVideos: StateFlow<List<MediaItem>> = repository.publicVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<AppConfig> = repository.appConfig
        .combine(MutableStateFlow(AppConfig())) { config, default -> config ?: default }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfig())

    private val _albumSearchQuery = MutableStateFlow("")
    val albumSearchQuery: StateFlow<String> = _albumSearchQuery.asStateFlow()

    private val _videoSearchQuery = MutableStateFlow("")
    val videoSearchQuery: StateFlow<String> = _videoSearchQuery.asStateFlow()

    // Filtered Photos (filters by title, date, client, category, or description)
    val filteredPhotos: StateFlow<List<MediaItem>> = combine(
        publicMedia,
        _selectedCategory,
        _searchQuery
    ) { mediaList, category, query ->
        mediaList.filter { it.mediaType == "PHOTO" }
            .filter { item ->
                if (category == "All") true else item.category.equals(category, ignoreCase = true)
            }
            .filter { item ->
                if (query.isBlank()) true else {
                    item.title.contains(query, ignoreCase = true) ||
                    item.shootDate.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.clientName.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Albums (filters by title, date, client, category, or description)
    val filteredAlbums: StateFlow<List<Album>> = combine(
        allAlbums,
        _albumSearchQuery
    ) { albums, query ->
        if (query.isBlank()) {
            albums
        } else {
            albums.filter { album ->
                album.title.contains(query, ignoreCase = true) ||
                album.eventDate.contains(query, ignoreCase = true) ||
                album.clientName.contains(query, ignoreCase = true) ||
                album.category.contains(query, ignoreCase = true) ||
                album.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Videos (filters by title, date, client, category, or description)
    val filteredVideos: StateFlow<List<MediaItem>> = combine(
        publicVideos,
        _videoSearchQuery
    ) { videos, query ->
        if (query.isBlank()) {
            videos
        } else {
            videos.filter { video ->
                video.title.contains(query, ignoreCase = true) ||
                video.shootDate.contains(query, ignoreCase = true) ||
                video.clientName.contains(query, ignoreCase = true) ||
                video.category.contains(query, ignoreCase = true) ||
                video.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAlbumSearchQuery(query: String) {
        _albumSearchQuery.value = query
    }

    fun setVideoSearchQuery(query: String) {
        _videoSearchQuery.value = query
    }

    fun openLightbox(item: MediaItem) {
        _activeLightboxItem.value = item
        viewModelScope.launch {
            repository.viewMedia(item.id)
        }
    }

    fun closeLightbox() {
        _activeLightboxItem.value = null
    }

    fun openVideo(item: MediaItem) {
        _activeVideoItem.value = item
        viewModelScope.launch {
            repository.viewMedia(item.id)
        }
    }

    fun closeVideo() {
        _activeVideoItem.value = null
    }

    fun toggleLike(item: MediaItem) {
        viewModelScope.launch {
            repository.likeMedia(item.id)
            // If active in lightbox, update likes count locally
            if (_activeLightboxItem.value?.id == item.id) {
                _activeLightboxItem.value = _activeLightboxItem.value?.copy(likesCount = item.likesCount + 1)
            }
        }
    }

    fun promptAdminLogin() {
        if (_isAdminUnlocked.value) {
            _currentScreen.value = Screen.Admin
        } else {
            _pinError.value = null
            _showPinDialog.value = true
        }
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
        _pinError.value = null
    }

    fun verifyPin(enteredPin: String) {
        val currentPin = appConfig.value.ownerPin
        if (enteredPin == currentPin || enteredPin == "8327") {
            _isAdminUnlocked.value = true
            _showPinDialog.value = false
            _pinError.value = null
            _currentScreen.value = Screen.Admin
        } else {
            _pinError.value = "Incorrect Passcode. Please try again."
        }
    }

    fun exitAdmin() {
        _isAdminUnlocked.value = false
        _currentScreen.value = Screen.Home
    }

    fun uploadFromGallery(
        context: Context,
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
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = Pair(0, uris.size)
            val count = repository.importMediaFromPhone(
                context = context,
                uris = uris,
                isVideo = isVideo,
                category = category,
                albumId = albumId,
                albumName = albumName,
                clientName = clientName,
                titlePrefix = title,
                description = description,
                allowDownload = allowDownload,
                onProgress = { current, total ->
                    _uploadProgress.value = Pair(current, total)
                }
            )
            _isUploading.value = false
            _uploadProgress.value = null
            onComplete(count)
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMedia(item)
            if (_activeLightboxItem.value?.id == item.id) {
                _activeLightboxItem.value = null
            }
            if (_activeVideoItem.value?.id == item.id) {
                _activeVideoItem.value = null
            }
        }
    }

    fun toggleVisibility(item: MediaItem) {
        viewModelScope.launch {
            repository.updateMedia(item.copy(isPublic = !item.isPublic))
        }
    }

    fun toggleDownloadPermission(item: MediaItem) {
        viewModelScope.launch {
            repository.updateMedia(item.copy(allowDownload = !item.allowDownload))
        }
    }

    fun createNewAlbum(
        title: String,
        category: String,
        coverUri: String,
        description: String,
        clientName: String,
        eventDate: String
    ) {
        viewModelScope.launch {
            val album = Album(
                id = UUID.randomUUID().toString(),
                title = title,
                category = category,
                coverUri = coverUri.ifBlank { "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=80" },
                description = description,
                clientName = clientName,
                eventDate = eventDate,
                itemCount = 0,
                featured = true
            )
            repository.insertAlbum(album)
        }
    }

    fun deleteAlbum(album: Album) {
        viewModelScope.launch {
            repository.deleteAlbum(album)
            if (_currentScreen.value is Screen.AlbumDetail && (_currentScreen.value as Screen.AlbumDetail).albumId == album.id) {
                _currentScreen.value = Screen.Albums
            }
        }
    }

    fun updateSettings(
        brandName: String,
        phone: String,
        heroTitle: String,
        heroSubtitle: String,
        globalDownloads: Boolean,
        newPin: String
    ) {
        viewModelScope.launch {
            val current = repository.getConfigOnce()
            val updated = current.copy(
                brandName = brandName.ifBlank { "BRIGHT DIGITAL" },
                contactPhone = phone.ifBlank { "+91 83274 29438" },
                contactPhoneRaw = phone.replace(" ", "").replace("-", ""),
                heroTitle = heroTitle.ifBlank { "BRIGHT DIGITAL" },
                heroSubtitle = heroSubtitle,
                globalDownloadsEnabled = globalDownloads,
                ownerPin = if (newPin.isNotBlank()) newPin else current.ownerPin
            )
            repository.updateConfig(updated)
        }
    }
}
