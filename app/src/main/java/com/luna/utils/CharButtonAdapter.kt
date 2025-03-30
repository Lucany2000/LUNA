import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luna.data.Song

import com.luna.main.R
import com.luna.utils.BackEnd
import com.luna.utils.UI

class CharButtonAdapter(
    private val context: Context,
    private var uniqueChars: List<Char>,
    private var letterToFirstInstance: Map<String, Int>,
    private val scrollToFirstInstance: (Int) -> Unit // Function to scroll to song
) : RecyclerView.Adapter<CharButtonAdapter.CharViewHolder>() {

    private var popUpWindow: PopupWindow? = null

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

        holder.button.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {

                    popUpWindow = UI.showBubbleText(context, view, char)

                    holder.button.setBackgroundColor(colorPressed)
                    scrollToFirstInstance(letterToFirstInstance[char]?: return@setOnTouchListener false)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    popUpWindow?.dismiss()

                    holder.button.setBackgroundColor(currentColor)
                    true
                }
                else -> false
            }
        }
    }

    override fun getItemCount() = uniqueChars.size

    fun updateCharAdapter(newUniqueChars: MutableList<Char>, newLetterToFirstInstance: MutableMap<String, Int>) {
        letterToFirstInstance.toMutableMap().clear()
        letterToFirstInstance = newLetterToFirstInstance.toMap()

        uniqueChars.toMutableList().clear()
        uniqueChars = newUniqueChars.toList()

        notifyDataSetChanged()
    }
}