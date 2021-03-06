package com.maxclub.android.hellobluetooth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Controller::class, Widget::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class MyControllerDatabase : RoomDatabase() {
    abstract fun myControllerDao(): MyControllerDao

    companion object {
        @Volatile
        private var INSTANCE: MyControllerDatabase? = null

        fun getDatabase(context: Context): MyControllerDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MyControllerDatabase::class.java,
                    "my_controller_db"
                ).addCallback(databaseCallback)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                        CREATE TRIGGER set_controller_order_value_trigger AFTER INSERT ON controller_table
                        BEGIN
                            UPDATE controller_table 
                            SET `order` = (SELECT MAX(`order`) + 1 FROM controller_table) 
                            WHERE id = NEW.id;
                        END;
                    """.trimIndent()
                )

                db.execSQL(
                    """
                        CREATE TRIGGER set_widget_order_value_trigger AFTER INSERT ON widget_table
                        BEGIN
                            UPDATE widget_table 
                            SET `order` = (SELECT MAX(`order`) + 1 FROM widget_table WHERE controller_id = NEW.controller_id) 
                            WHERE id = NEW.id AND `order` = -1;
                        END;
                    """.trimIndent()
                )
            }
        }
    }
}