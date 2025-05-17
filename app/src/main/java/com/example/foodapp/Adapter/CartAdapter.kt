package com.example.foodapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.databinding.ViewholderCartBinding
import com.example.project1762.Helper.ManagmentCart
import com.uilover.project195.Helper.ChangeNumberItemsListener

class CartAdapter (
    private val listItemSelected:ArrayList<ItemsModel>,
    context: Context,
    val changeNumberItemsListener:ChangeNumberItemsListener?=null
):RecyclerView.Adapter<CartAdapter.ViewHolder>()
{
    class ViewHolder(val binding:ViewholderCartBinding):RecyclerView.ViewHolder(binding.root)

    private val managmentCart = ManagmentCart(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ViewHolder {
        val binding=ViewholderCartBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {
        val item=listItemSelected[position]


        holder.binding.titleTxt.text=item.drinkName
        holder.binding.sizeTxt.text=item.size
        holder.binding.feeEachItem.text="$${item.drinkPrice}"
        holder.binding.totalEachItem.text = "$${Math.round((item.numberInCart * (item.drinkPrice ?: 0.0)))}"
        holder.binding.numberItemTxt.text=item.numberInCart.toString()

        val imageUrl = item.drinkImage
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .apply (RequestOptions().transform(CenterCrop()))
            .into(holder.binding.picCart)

        holder.binding.plusEachItem.setOnClickListener{
            managmentCart.plusItem(listItemSelected,position,object :ChangeNumberItemsListener{
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener?.onChanged()
                }
            })
        }
        holder.binding.minusEachItem.setOnClickListener{
            managmentCart.minusItem(listItemSelected,position,object :ChangeNumberItemsListener{
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener?.onChanged()
                }
            })
        }

//        holder.itemView.setOnClickListener {
//           onItemClick?.invoke(item)
//        }

        holder.binding.removeItemBtn.setOnClickListener {
            managmentCart.romveItem(listItemSelected,position,object :ChangeNumberItemsListener{
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener?.onChanged()
                }
            })
        }
    }

    override fun getItemCount(): Int = listItemSelected.size

}