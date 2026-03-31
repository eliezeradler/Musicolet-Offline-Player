package com.example.offlineplayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FolderAdapter(
    private var foldersList: List<String>,
    private val onFolderClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolderName: TextView = view.findViewById(R.id.tvFolderName)
        val tvFolderPath: TextView = view.findViewById(R.id.tvFolderPath)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FolderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false))

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folderPath = foldersList[position]
        holder.tvFolderName.text = File(folderPath).name
        holder.tvFolderPath.text = folderPath
        holder.itemView.setOnClickListener { onFolderClick(folderPath) }
        holder.itemView.setOnFocusChangeListener { _, hasFocus -> holder.tvFolderName.isSelected = hasFocus }
    }

    override fun getItemCount() = foldersList.size
    fun updateData(newFolders: List<String>) { foldersList = newFolders; notifyDataSetChanged() }
}


