package com.example.roomexample

import androidx.room.*

@Dao
interface ContactsDao {
    @Query("SELECT * FROM tb_contacts")
    fun getAll(): List<Contacts>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg contacts: Contacts)

    @Delete
    fun delete(contacts: Contacts)
}