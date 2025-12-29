import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class AlbumsFragment : Fragment(R.layout.fragment_albums) {

    private lateinit var query: QueryTable

    private lateinit var albumAdapter: RecycleViewAdapter<Pair<String, String>>
    private lateinit var charAdapter: CharButtonAdapter

    private lateinit var buttonCreation: ButtonCreation

    companion object {
        private const val ARTIST_NAME = "artist_name"

        // The "Vending Machine" (Static Factory)
        @JvmStatic
        fun newInstance(artist: String) = AlbumsFragment().apply {
            arguments = Bundle().apply {
                putString(ARTIST_NAME, artist)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
//
        query = QueryTable(context)
        buttonCreation = object : ButtonCreation(context as AppCompatActivity) {}

        // 1. Unpack the "Suitcase"
        val artistName = arguments?.getString(ARTIST_NAME) ?: "Unknown Artist"

        val charRecyclerView: RecyclerView = view.findViewById(R.id.charRecyclerView)
        val albumRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView)


        charRecyclerView.layoutManager = LinearLayoutManager(context)
        albumRecyclerView.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val readOnlyDB = query.readOnlyMode()
            val albumList = query.getAlbums(readOnlyDB, artistName).toMutableList()
            albumList.add(Pair("All", artistName))

            val generatedAlbumList = BackEnd.sort(albumList)

            val uniqueChars = BackEnd.createKnownAlphabet(generatedAlbumList).toMutableList()

            val letterToFirstInstance = mutableMapOf<String, Int>()

            generatedAlbumList.forEachIndexed { index, album ->
                val title = album.first
                val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

                if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                    letterToFirstInstance[firstChar] = index
                }
            }

            withContext(Dispatchers.Main) {
                albumAdapter = RecycleViewAdapter(context, generatedAlbumList) { album ->
                    buttonCreation.createAlbumButton(context, album)
                }
                albumRecyclerView.adapter = albumAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                charAdapter =
                    CharButtonAdapter(context, uniqueChars, letterToFirstInstance) { position ->
                        albumRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                    }
                charRecyclerView.adapter = charAdapter

            }
        }
    }
}
