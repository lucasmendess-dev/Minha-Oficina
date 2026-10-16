package br.com.minhaoficina.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FerramentaDao {
    @Query("SELECT * FROM ferramentas ORDER BY nome COLLATE NOCASE")
    fun todas(): Flow<List<Ferramenta>>

    @Upsert
    suspend fun salvar(ferramenta: Ferramenta): Long

    @Delete
    suspend fun apagar(ferramenta: Ferramenta)

    /** Ao apagar um kit, as ferramentas dele voltam a ser avulsas. */
    @Query("UPDATE ferramentas SET kitId = NULL WHERE kitId = :kitId")
    suspend fun soltarDoKit(kitId: Long)
}
