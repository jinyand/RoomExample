package com.example.roomexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var db : AppDatabase? = null
    var contactsList = mutableListOf<Contacts>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this) // 초기화

        // 이전에 저장한 내용 모두 불러와서 추가
        val savedContacts = db!!.contactsDao().getAll()
        if (savedContacts.isNotEmpty()) {
            contactsList.addAll(savedContacts)
        }

        val adapter = ContactsListAdapter(contactsList)
        adapter.setItemClickListener(object: ContactsListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val contacts = contactsList[position]

                db?.contactsDao()?.delete(contacts = contacts) // DB에서 삭제
                contactsList.removeAt(position) // 리스트에서 삭제
                adapter.notifyDataSetChanged() // 리사이클러뷰 갱신
            }
        })

        rv_contacts.adapter = adapter

        // 랜덤으로 만든 리스트 아이템 추가
        btn_add.setOnClickListener {
            val random = Random()
            val numA = random.nextInt(1000)
            val numB = random.nextInt(10000)
            val numC = random.nextInt(10000)
            val rndNumber = String.format("%03d-%04d-%04d",numA,numB,numC)

            val contact = Contacts(0, "New $numA", rndNumber) //Contacts 생성
            db?.contactsDao()?.insertAll(contact) //DB에 추가
            contactsList.add(contact) //리스트 추가

            adapter.notifyDataSetChanged() //리스트뷰 갱신
        }
    }
}