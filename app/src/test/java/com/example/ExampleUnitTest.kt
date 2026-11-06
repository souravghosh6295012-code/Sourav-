package com.example

import com.example.data.model.Album
import com.example.data.model.AppConfig
import com.example.data.model.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun appConfig_defaults_areAccurate() {
        val config = AppConfig()
        assertEquals("BRIGHT DIGITAL", config.brandName)
        assertEquals("+91 83274 29438", config.contactPhone)
        assertEquals("+918327429438", config.contactPhoneRaw)
        assertEquals("8327", config.ownerPin)
        assertTrue(config.globalDownloadsEnabled)
        assertFalse(config.brandName.contains("Studio", ignoreCase = true))

        val categories = config.getCategories()
        assertEquals(6, categories.size)
        assertTrue(categories.contains("Wedding"))
        assertTrue(categories.contains("Birthday"))
        assertTrue(categories.contains("Annaprashan"))
        assertTrue(categories.contains("Baby"))
        assertTrue(categories.contains("Portrait"))
        assertTrue(categories.contains("Nature"))
    }

    @Test
    fun mediaItem_properties_areCorrect() {
        val photo = MediaItem(
            id = "test_photo_1",
            title = "Royal Haldi Moment",
            description = "Traditional ceremony",
            mediaType = "PHOTO",
            uri = "https://example.com/haldi.jpg",
            category = "Wedding",
            clientName = "Rahul & Priya",
            shootDate = "24 Oct 2026",
            isPublic = true,
            allowDownload = true
        )
        assertEquals("PHOTO", photo.mediaType)
        assertEquals("Wedding", photo.category)
        assertEquals("24 Oct 2026", photo.shootDate)
        assertTrue(photo.isPublic)
        assertTrue(photo.allowDownload)
    }

    @Test
    fun album_properties_areCorrect() {
        val album = Album(
            id = "album_test_1",
            title = "Priya & Rahul Wedding",
            category = "Wedding",
            coverUri = "https://example.com/cover.jpg",
            clientName = "Rahul & Priya",
            eventDate = "October 2026",
            itemCount = 12
        )
        assertEquals("Wedding", album.category)
        assertEquals("October 2026", album.eventDate)
        assertEquals(12, album.itemCount)
    }

    @Test
    fun filterAlbums_byTitleOrDate_worksCorrectly() {
        val albums = listOf(
            Album(
                id = "1",
                title = "Raj & Ananya Royal Wedding",
                category = "Wedding",
                coverUri = "",
                eventDate = "October 2026"
            ),
            Album(
                id = "2",
                title = "Aarav's Annaprashan Ceremony",
                category = "Annaprashan",
                coverUri = "",
                eventDate = "September 2026"
            ),
            Album(
                id = "3",
                title = "Little Diya 1st Birthday Bash",
                category = "Birthday",
                coverUri = "",
                eventDate = "August 2026"
            )
        )

        // Filter by title
        val weddingResults = albums.filter {
            it.title.contains("Wedding", ignoreCase = true) || it.eventDate.contains("Wedding", ignoreCase = true)
        }
        assertEquals(1, weddingResults.size)
        assertEquals("Raj & Ananya Royal Wedding", weddingResults[0].title)

        // Filter by date
        val septemberResults = albums.filter {
            it.title.contains("September", ignoreCase = true) || it.eventDate.contains("September", ignoreCase = true)
        }
        assertEquals(1, septemberResults.size)
        assertEquals("Aarav's Annaprashan Ceremony", septemberResults[0].title)

        // Filter by year
        val yearResults = albums.filter {
            it.title.contains("2026", ignoreCase = true) || it.eventDate.contains("2026", ignoreCase = true)
        }
        assertEquals(3, yearResults.size)
    }

    @Test
    fun filterMedia_byTitleOrDate_worksCorrectly() {
        val mediaList = listOf(
            MediaItem(
                id = "1",
                title = "Royal Sindoor Daan",
                description = "Sacred ritual",
                mediaType = "PHOTO",
                uri = "",
                category = "Wedding",
                shootDate = "24 Oct 2026"
            ),
            MediaItem(
                id = "2",
                title = "Mukhe Bhaat Payesh",
                description = "Rice ceremony",
                mediaType = "PHOTO",
                uri = "",
                category = "Annaprashan",
                shootDate = "08 Sep 2026"
            ),
            MediaItem(
                id = "3",
                title = "Cake Smash Fun",
                description = "Birthday party",
                mediaType = "PHOTO",
                uri = "",
                category = "Birthday",
                shootDate = "15 Aug 2026"
            )
        )

        // Filter by title
        val sindoorMatches = mediaList.filter {
            it.title.contains("Sindoor", ignoreCase = true) || it.shootDate.contains("Sindoor", ignoreCase = true)
        }
        assertEquals(1, sindoorMatches.size)
        assertEquals("Royal Sindoor Daan", sindoorMatches[0].title)

        // Filter by date "Oct"
        val octMatches = mediaList.filter {
            it.title.contains("Oct", ignoreCase = true) || it.shootDate.contains("Oct", ignoreCase = true)
        }
        assertEquals(1, octMatches.size)
        assertEquals("24 Oct 2026", octMatches[0].shootDate)
    }
}
