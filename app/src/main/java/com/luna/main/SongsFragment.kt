import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luna.data.Song
import com.luna.main.R
import com.luna.utils.BackEnd
import com.luna.utils.ButtonCreation
import com.luna.utils.QueryTable
import com.luna.utils.RecycleViewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.set

class SongsFragment : Fragment(R.layout.fragment_songs) {

    private lateinit var query: QueryTable

    private lateinit var songAdapter: RecycleViewAdapter<Song>
    private lateinit var charAdapter: CharButtonAdapter

    private lateinit var buttonCreation: ButtonCreation

    companion object {
        private const val ARTIST_NAME = "artist_name"
        private const val ALBUM_NAME = "album_name"

        // The "Vending Machine" (Static Factory)
        @JvmStatic
        fun newInstance(artist: String, album: String) = SongsFragment().apply {
            arguments = Bundle().apply {
                putString(ARTIST_NAME, artist)
                putString(ALBUM_NAME, album)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        query = QueryTable(context)
        buttonCreation = object : ButtonCreation(context as AppCompatActivity) {}

        // 1. Unpack the "Suitcase"
        val artistName = arguments?.getString(ARTIST_NAME) ?: "Unknown Artist"
        val albumName = arguments?.getString(ALBUM_NAME) ?: "Unknown Album"

        val charRecyclerView: RecyclerView = view.findViewById(R.id.charRecyclerView)
        val songRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView)


        charRecyclerView.layoutManager = LinearLayoutManager(context)
        songRecyclerView.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val readOnlyDB = query.readOnlyMode()

            val songList = if (albumName == "All") {
                query.getSongs(readOnlyDB, artistName)
            } else {
                query.getSongs(readOnlyDB, artistName, albumName)
            }

            val generatedSongList = BackEnd.sort(songList)

            val uniqueChars = BackEnd.createKnownAlphabet(generatedSongList).toMutableList()

            val letterToFirstInstance = mutableMapOf<String, Int>()

            generatedSongList.forEachIndexed { index, song ->
                val title = song.getTitle()
                val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

                if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                    letterToFirstInstance[firstChar] = index
                }
            }

            withContext(Dispatchers.Main) {
                songAdapter = RecycleViewAdapter(context, generatedSongList) { song ->
                    buttonCreation.createSongButton(context, song)
                }
                songRecyclerView.adapter = songAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                charAdapter =
                    CharButtonAdapter(context, uniqueChars, letterToFirstInstance) { position ->
                        songRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                    }
                charRecyclerView.adapter = charAdapter

            }
        }
    }
}