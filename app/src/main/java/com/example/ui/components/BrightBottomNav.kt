package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BrightBottomNav(
    currentScreen: Screen,
    isAdminUnlocked: Boolean,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = mutableListOf(
        NavItem(Screen.Home, "Home", Icons.Default.Home, "nav_home"),
        NavItem(Screen.Photos, "Photos", Icons.Default.PhotoLibrary, "nav_photos"),
        NavItem(Screen.Videos, "Videos", Icons.Default.Videocam, "nav_videos"),
        NavItem(Screen.Albums, "Albums", Icons.Default.Collections, "nav_albums"),
        NavItem(Screen.About, "Contact", Icons.Default.Info, "nav_contact")
    )

    if (isAdminUnlocked) {
        items.add(NavItem(Screen.Admin, "Admin", Icons.Default.AdminPanelSettings, "nav_admin"))
    }

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BlackBorder)
            .navigationBarsPadding(),
        containerColor = BlackSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = when (item.screen) {
                is Screen.Home -> currentScreen is Screen.Home
                is Screen.Photos -> currentScreen is Screen.Photos
                is Screen.Videos -> currentScreen is Screen.Videos
                is Screen.Albums -> currentScreen is Screen.Albums || currentScreen is Screen.AlbumDetail
                is Screen.About -> currentScreen is Screen.About
                is Screen.Admin -> currentScreen is Screen.Admin
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = GoldPrimary,
                    indicatorColor = GoldPrimary,
                    unselectedIconColor = TextDim,
                    unselectedTextColor = TextDim
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
