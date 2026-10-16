package br.com.minhaoficina.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Um kit agrupa várias ferramentas (ex.: "Kit chave de boca" com Chave 01, 02, 03). */
@Entity(tableName = "kits")
data class Kit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String = "",
    val local: String = "",
    val criadoEm: Long = System.currentTimeMillis(),
)

@Dao
interface KitDao {
    @Query("SELECT * FROM kits ORDER BY nome COLLATE NOCASE")
    fun todos(): Flow<List<Kit>>

    @Upsert
    suspend fun salvar(kit: Kit): Long

    @Delete
    suspend fun apagar(kit: Kit)
}
