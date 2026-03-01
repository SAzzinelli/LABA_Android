package com.laba.firenze.data.repository

import android.content.Context
import com.laba.firenze.data.api.GestionaleApi
import com.laba.firenze.data.api.GestionaleLoginRequest
import com.laba.firenze.data.api.GestionaleUser
import com.laba.firenze.domain.model.CreateEquipmentReport
import com.laba.firenze.domain.model.CreateEquipmentRequest
import com.laba.firenze.domain.model.EquipmentLoan
import com.laba.firenze.domain.model.EquipmentReport
import com.laba.firenze.domain.model.EquipmentRequest
import com.laba.firenze.domain.model.UserEquipment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import androidx.core.content.edit

/** Repository Service LABA (Gestionale) - identico a iOS LABAGestionaleTokenManager + NetworkManager. */
@Singleton
class GestionaleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: GestionaleApi
) {
    private val prefs = context.getSharedPreferences("laba_gestionale", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mutex = Mutex()
    
    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null) ?: "")
    val token: StateFlow<String> = _token.asStateFlow()
    
    private val _user = MutableStateFlow(loadUserFromPrefs())
    val user: StateFlow<GestionaleUser?> = _user.asStateFlow()
    
    val isAuthenticated: Boolean
        get() = _token.value.isNotEmpty()
    
    private fun loadUserFromPrefs(): GestionaleUser? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, GestionaleUser::class.java)
        } catch (_: Exception) {
            null
        }
    }
    
    suspend fun login(email: String, password: String): Result<GestionaleUser> = mutex.withLock {
        return try {
            val response = api.login(GestionaleLoginRequest(email = email.trim().lowercase(), password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    prefs.edit {
                        putString(KEY_TOKEN, body.token)
                        putString(KEY_USER, gson.toJson(body.user))
                    }
                    _token.value = body.token
                    _user.value = body.user
                    Result.success(body.user)
                } else {
                    Result.failure(Exception("Risposta vuota"))
                }
            } else {
                val msg = response.errorBody()?.string() ?: "Errore ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER)
        }
        _token.value = ""
        _user.value = null
    }
    
    suspend fun getCurrentUser(): Result<GestionaleUser> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.getMe("Bearer $t")
            if (response.isSuccessful) {
                val u = response.body()
                if (u != null) {
                    prefs.edit { putString(KEY_USER, gson.toJson(u)) }
                    _user.value = u
                    Result.success(u)
                } else Result.failure(Exception("Risposta vuota"))
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableEquipment(): Result<List<UserEquipment>> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.getAvailableEquipment("Bearer $t")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRequests(): Result<List<EquipmentRequest>> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.getUserRequests("Bearer $t")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRequest(request: CreateEquipmentRequest): Result<EquipmentRequest> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.createRequest("Bearer $t", request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Risposta vuota"))
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserLoans(): Result<List<EquipmentLoan>> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.getUserLoans("Bearer $t")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserReports(): Result<List<EquipmentReport>> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.getUserReports("Bearer $t")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createReport(report: CreateEquipmentReport): Result<EquipmentReport> {
        val t = _token.value
        if (t.isEmpty()) return Result.failure(Exception("Non autenticato"))
        return try {
            val response = api.createReport("Bearer $t", report)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Risposta vuota"))
            } else {
                if (response.code() == 401) logout()
                Result.failure(Exception(response.errorBody()?.string() ?: "Errore ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val KEY_TOKEN = "gestionale_token"
        private const val KEY_USER = "gestionale_user"
    }
}
