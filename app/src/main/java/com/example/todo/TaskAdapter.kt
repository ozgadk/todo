package src
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.Task
import com.example.todo.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onCompleteToggle: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                textViewTitle.text = task.title
                textViewDescription.text = if (task.description.isNotEmpty()) task.description else "No Description"
                textViewDate.text = "Created: ${dateFormat.format(Date(task.createdAt))}"
                checkBoxCompleted.isChecked = task.isCompleted

                if (task.isCompleted) {
                    textViewTitle.paintFlags = textViewTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textViewTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.completed_text))
                    textViewDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.completed_text))
                    cardView.alpha = 0.7f
                } else {
                    textViewTitle.paintFlags = textViewTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textViewTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))
                    textViewDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_text))
                    cardView.alpha = 1.0f
                }

                if (task.updatedAt != task.createdAt) {
                    textViewDate.text = "${textViewDate.text}\nUpdatded: ${dateFormat.format(Date(task.updatedAt))}"
                }
            }

            binding.root.setOnClickListener { onTaskClick(task) }
            binding.imageButtonDelete.setOnClickListener { onDeleteClick(task) }
            binding.checkBoxCompleted.setOnClickListener { onCompleteToggle(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}