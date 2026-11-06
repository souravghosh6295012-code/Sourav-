package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Album
import com.example.data.model.AppConfig
import com.example.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MediaItem::class, Album::class, AppConfig::class],
    version = 1,
    exportSchema = false
)
abstract class BrightDigitalDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun albumDao(): AlbumDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: BrightDigitalDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BrightDigitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrightDigitalDatabase::class.java,
                    "bright_digital_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.mediaDao(), database.albumDao(), database.configDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(
            mediaDao: MediaDao,
            albumDao: AlbumDao,
            configDao: ConfigDao
        ) {
            // Seed initial config
            configDao.insertConfig(
                AppConfig(
                    key = "primary_config",
                    ownerPin = "8327",
                    brandName = "BRIGHT DIGITAL",
                    contactPhone = "+91 83274 29438",
                    contactPhoneRaw = "+918327429438",
                    heroTitle = "BRIGHT DIGITAL",
                    heroSubtitle = "Cinematic Photography & Videography Portfolio",
                    globalDownloadsEnabled = true,
                    watermarkEnabled = false,
                    categoriesJson = "Wedding,Birthday,Annaprashan,Baby,Portrait,Nature"
                )
            )

            // Seed initial Albums
            val albums = listOf(
                Album(
                    id = "album_1",
                    title = "Raj & Ananya Royal Wedding",
                    category = "Wedding",
                    coverUri = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=80",
                    description = "Royal heritage wedding celebration in Kolkata with traditional Bengali rituals and grand reception.",
                    clientName = "Raj & Ananya",
                    eventDate = "October 2026",
                    itemCount = 6,
                    featured = true
                ),
                Album(
                    id = "album_2",
                    title = "Aarav's Annaprashan Ceremony",
                    category = "Annaprashan",
                    coverUri = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=1200&q=80",
                    description = "Traditional Bengali first rice eating ceremony (Mukhe Bhaat) filled with family blessings and sacred rituals.",
                    clientName = "Dr. S. Banerjee",
                    eventDate = "September 2026",
                    itemCount = 4,
                    featured = true
                ),
                Album(
                    id = "album_3",
                    title = "Little Diya 1st Birthday Bash",
                    category = "Birthday",
                    coverUri = "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?auto=format&fit=crop&w=1200&q=80",
                    description = "Magical fairy-tale themed 1st birthday party with cake smash and candid joyful moments.",
                    clientName = "Roy Family",
                    eventDate = "August 2026",
                    itemCount = 5,
                    featured = true
                ),
                Album(
                    id = "album_4",
                    title = "Golden Hour Newborn & Baby",
                    category = "Baby",
                    coverUri = "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=1200&q=80",
                    description = "Delicate and pure newborn baby portraits captured in cozy natural lighting.",
                    clientName = "Chatterjee Family",
                    eventDate = "November 2026",
                    itemCount = 4,
                    featured = true
                )
            )
            albumDao.insertAll(albums)

            // Seed initial Media Items (Photos and Videos across all requested categories)
            val mediaItems = listOf(
                // Wedding Photography
                MediaItem(
                    id = "p_w1",
                    title = "Royal Sindoor Daan Ritual",
                    description = "The sacred culmination of the traditional Bengali wedding ceremony bathed in warm ambient lighting.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=1200&q=80",
                    category = "Wedding",
                    albumId = "album_1",
                    albumName = "Raj & Ananya Royal Wedding",
                    clientName = "Raj & Ananya",
                    shootDate = "24 Oct 2026",
                    featured = true,
                    viewsCount = 342,
                    likesCount = 89
                ),
                MediaItem(
                    id = "p_w2",
                    title = "Candid Bridal Portrait",
                    description = "Stunning Banarasi silk saree and exquisite traditional gold jewellery captured with 85mm prime lens.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1200&q=80",
                    category = "Wedding",
                    albumId = "album_1",
                    albumName = "Raj & Ananya Royal Wedding",
                    clientName = "Raj & Ananya",
                    shootDate = "24 Oct 2026",
                    featured = true,
                    viewsCount = 420,
                    likesCount = 112
                ),
                MediaItem(
                    id = "p_w3",
                    title = "Varmala & Flower Shower",
                    description = "Energetic moment of bride and groom exchanging floral garlands amidst cheering friends and family.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=1200&q=80",
                    category = "Wedding",
                    albumId = "album_1",
                    albumName = "Raj & Ananya Royal Wedding",
                    clientName = "Raj & Ananya",
                    shootDate = "24 Oct 2026",
                    viewsCount = 280,
                    likesCount = 76
                ),

                // Birthday Photography
                MediaItem(
                    id = "p_b1",
                    title = "The 1st Cake Smash Fiesta",
                    description = "Pure delight as the birthday princess discovers frosting and celebrates turning one year old.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1513151233558-d860c5398176?auto=format&fit=crop&w=1200&q=80",
                    category = "Birthday",
                    albumId = "album_3",
                    albumName = "Little Diya 1st Birthday Bash",
                    clientName = "Roy Family",
                    shootDate = "15 Aug 2026",
                    featured = true,
                    viewsCount = 310,
                    likesCount = 94
                ),
                MediaItem(
                    id = "p_b2",
                    title = "Balloons & Candle Glow",
                    description = "Golden bokeh backdrop with pastel balloon arch and radiant laughter.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=1200&q=80",
                    category = "Birthday",
                    albumId = "album_3",
                    albumName = "Little Diya 1st Birthday Bash",
                    clientName = "Roy Family",
                    shootDate = "15 Aug 2026",
                    viewsCount = 195,
                    likesCount = 48
                ),

                // Annaprashan Photography
                MediaItem(
                    id = "p_a1",
                    title = "Mukhe Bhaat Blessing",
                    description = "Little prince dressed in silk dhoti and topor tasting payesh from maternal uncle's hand.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=1200&q=80",
                    category = "Annaprashan",
                    albumId = "album_2",
                    albumName = "Aarav's Annaprashan Ceremony",
                    clientName = "Dr. S. Banerjee",
                    shootDate = "08 Sep 2026",
                    featured = true,
                    viewsCount = 450,
                    likesCount = 135
                ),
                MediaItem(
                    id = "p_a2",
                    title = "Silver Thali & Sacred Puja",
                    description = "Traditional rituals, conch blowing, and sacred incense blessings during the ceremony.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1590073242678-70ee3fc28e8e?auto=format&fit=crop&w=1200&q=80",
                    category = "Annaprashan",
                    albumId = "album_2",
                    albumName = "Aarav's Annaprashan Ceremony",
                    clientName = "Dr. S. Banerjee",
                    shootDate = "08 Sep 2026",
                    viewsCount = 220,
                    likesCount = 63
                ),

                // Baby Photography
                MediaItem(
                    id = "p_bb1",
                    title = "Dreamy Newborn Slumber",
                    description = "Soft organic cotton wrap in natural morning light, capturing the tranquility of early days.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=1200&q=80",
                    category = "Baby",
                    albumId = "album_4",
                    albumName = "Golden Hour Newborn & Baby",
                    clientName = "Chatterjee Family",
                    shootDate = "02 Nov 2026",
                    featured = true,
                    viewsCount = 380,
                    likesCount = 104
                ),
                MediaItem(
                    id = "p_bb2",
                    title = "Tiny Fingers & Little Toes",
                    description = "Artistic macro shot highlighting precious newborn innocence with shallow depth of field.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?auto=format&fit=crop&w=1200&q=80",
                    category = "Baby",
                    albumId = "album_4",
                    albumName = "Golden Hour Newborn & Baby",
                    clientName = "Chatterjee Family",
                    shootDate = "02 Nov 2026",
                    viewsCount = 265,
                    likesCount = 82
                ),

                // Portrait Photography
                MediaItem(
                    id = "p_pt1",
                    title = "Golden Hour Rim Light",
                    description = "Editorial outdoor portrait with sunset backlight and natural cinematic expression.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80",
                    category = "Portrait",
                    clientName = "Sohini M.",
                    shootDate = "18 Oct 2026",
                    featured = true,
                    viewsCount = 520,
                    likesCount = 160
                ),
                MediaItem(
                    id = "p_pt2",
                    title = "Monochrome Portrait Elegance",
                    description = "High-contrast chiaroscuro lighting capturing raw emotional depth and timeless elegance.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=1200&q=80",
                    category = "Portrait",
                    clientName = "Arijit D.",
                    shootDate = "12 Oct 2026",
                    viewsCount = 310,
                    likesCount = 74
                ),

                // Nature Photography
                MediaItem(
                    id = "p_nt1",
                    title = "Misty Himalayan Sunrise",
                    description = "Golden dawn peaks cutting through morning fog in North Bengal hills with breathtaking clarity.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80",
                    category = "Nature",
                    shootDate = "05 Oct 2026",
                    featured = true,
                    viewsCount = 410,
                    likesCount = 128
                ),
                MediaItem(
                    id = "p_nt2",
                    title = "Sundarbans Golden Riverway",
                    description = "Serene backwaters mirroring the amber dusk sky and lush mangrove silhouettes.",
                    mediaType = "PHOTO",
                    uri = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80",
                    category = "Nature",
                    shootDate = "28 Sep 2026",
                    viewsCount = 330,
                    likesCount = 95
                ),

                // All Videos
                MediaItem(
                    id = "v_1",
                    title = "Raj & Ananya Cinematic Wedding Teaser",
                    description = "4K 60fps cinematic wedding film teaser featuring drone sweeps, emotional vows, and grand celebration.",
                    mediaType = "VIDEO",
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=80",
                    category = "Wedding",
                    albumId = "album_1",
                    albumName = "Raj & Ananya Royal Wedding",
                    clientName = "Raj & Ananya",
                    shootDate = "24 Oct 2026",
                    featured = true,
                    viewsCount = 920,
                    likesCount = 240
                ),
                MediaItem(
                    id = "v_2",
                    title = "Aarav's Annaprashan Highlights Reel",
                    description = "Emotional family montage of blessings, laughing baby smiles, and traditional conch melodies.",
                    mediaType = "VIDEO",
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=1200&q=80",
                    category = "Annaprashan",
                    albumId = "album_2",
                    albumName = "Aarav's Annaprashan Ceremony",
                    clientName = "Dr. S. Banerjee",
                    shootDate = "08 Sep 2026",
                    featured = true,
                    viewsCount = 650,
                    likesCount = 180
                ),
                MediaItem(
                    id = "v_3",
                    title = "Diya 1st Birthday Aftermovie",
                    description = "Joyful high-energy birthday party reel with confetti drops and cake celebration.",
                    mediaType = "VIDEO",
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?auto=format&fit=crop&w=1200&q=80",
                    category = "Birthday",
                    albumId = "album_3",
                    albumName = "Little Diya 1st Birthday Bash",
                    clientName = "Roy Family",
                    shootDate = "15 Aug 2026",
                    viewsCount = 510,
                    likesCount = 142
                )
            )
            mediaDao.insertAll(mediaItems)
        }
    }
}
