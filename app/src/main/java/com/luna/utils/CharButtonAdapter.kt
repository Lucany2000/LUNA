import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luna.main.R

class CharButtonAdapter(
    private val context: Context,
    private val uniqueChars: List<Char>,
    private val letterToFirstInstance: Map<String, Int>,
    private val scrollToFirstInstance: (Int) -> Unit // Function to scroll to song
) : RecyclerView.Adapter<CharButtonAdapter.CharViewHolder>() {

    class CharViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: TextView = view.findViewById(R.id.charButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.char_line, parent, false)
        return CharViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharViewHolder, position: Int) {
        val char = uniqueChars[position].toString()
        holder.button.text = char

        val currentColor = (holder.button.background as ColorDrawable).color
        val colorPressed = Color.BLUE

        holder.button.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    holder.button.setBackgroundColor(colorPressed)
                    scrollToFirstInstance(letterToFirstInstance[char]!!) // Scroll to song
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    holder.button.setBackgroundColor(currentColor)
                    true
                }
                else -> false
            }
        }
    }

    override fun getItemCount() = uniqueChars.size
}