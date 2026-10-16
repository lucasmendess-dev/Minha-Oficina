package br.com.minhaoficina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Ferramenta::class, Kit::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ferramentas(): FerramentaDao
    abstract fun kits(): KitDao

    companion object {
        /** Versão 1.3: cria a tabela de kits e o vínculo da ferramenta com o kit, mantendo os dados. */
        val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `ferramentas` ADD COLUMN `kitId` INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `kits` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nome` TEXT NOT NULL, " +
                        "`local` TEXT NOT NULL, " +
                        "`criadoEm` INTEGER NOT NULL)"
                )
            }
        }

        @Volatile private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oficina.db",
                ).addMigrations(MIGRACAO_1_2).build().also { instancia = it }
            }
    }
}
