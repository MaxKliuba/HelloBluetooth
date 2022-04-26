package com.maxclub.android.hellobluetooth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Controller::class, Widget::class], version = 1, exportSchema = false)
abstract class ControllerDatabase : RoomDatabase() {
    abstract fun controllerDao(): ControllerDao

    companion object {
        @Volatile
        private var INSTANCE: ControllerDatabase? = null

        fun getDatabase(context: Context): ControllerDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ControllerDatabase::class.java,
                    "controller_database"
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
                        UPDATE controller_table SET `order` = id;
                        END;
                    """.trimIndent()
                )

                db.execSQL(
                    """
                        CREATE TRIGGER set_widget_order_value_trigger AFTER INSERT ON widget_table
                        BEGIN
                        UPDATE widget_table SET `order` = id;
                        END;
                    """.trimIndent()
                )
            }
        }
    }
}