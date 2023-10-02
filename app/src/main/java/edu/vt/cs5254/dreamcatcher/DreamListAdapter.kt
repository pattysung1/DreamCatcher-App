package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding

class DreamHolder(private val binding: ListItemDreamBinding):
    RecyclerView.ViewHolder(binding.root){
    fun bind(dream : Dream){ //really just updateView!!!
        binding.listItemTitle.text = dream.title

        with(binding.listItemImage){
            when{
                dream.isFulfilled ->{
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_dream_fulfilled)
                }
                dream.isDeferred ->{
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_dream_deferred)
                }
                else ->{
                    visibility = View.GONE
                }
            }
        }
    }
}
class DreamListAdapter(private val dreams: List<Dream>) : RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)
        return  DreamHolder(binding)
    }

    override fun getItemCount(): Int {
        return dreams.size
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreams[position])
    }
}