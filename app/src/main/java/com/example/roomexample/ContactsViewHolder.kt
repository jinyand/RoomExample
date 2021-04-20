package com.example.roomexample

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_contacts.view.*

class ContactsViewHolder(v: View) : RecyclerView.ViewHolder(v){
    var view : View = v

    fun bind(item: Contacts) {
        view.tv_name.text = item.name
        view.tv_tel.text = item.tel
    }
}